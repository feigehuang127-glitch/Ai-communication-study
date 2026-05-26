package com.platform.service;

import com.platform.dto.AnswerSubmitRequest;
import com.platform.dto.GameResultResponse;
import com.platform.model.GameHistory;
import com.platform.model.Question;
import com.platform.model.User;
import com.platform.model.WrongQuestion;
import com.platform.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final QuestionRepository questionRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    private static final int QUESTIONS_PER_ROUND = 10;
    private static final int TIME_LIMIT_SECONDS = 10;
    private static final int TIMEOUT_EXEMPTIONS = 2;
    private static final long SESSION_TTL_MS = 30 * 60 * 1000; // 30 minutes

    public GameService(QuestionRepository questionRepository,
                       GameHistoryRepository gameHistoryRepository,
                       WrongQuestionRepository wrongQuestionRepository,
                       UserService userService,
                       UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.gameHistoryRepository = gameHistoryRepository;
        this.wrongQuestionRepository = wrongQuestionRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        List<String> expired = new ArrayList<>();
        for (var entry : activeSessions.entrySet()) {
            if (now - entry.getValue().getStartTime() > SESSION_TTL_MS) {
                expired.add(entry.getKey());
            }
        }
        for (String id : expired) {
            activeSessions.remove(id);
        }
        if (!expired.isEmpty()) {
            log.info("Cleaned up {} expired game sessions", expired.size());
        }
    }

    public GameSession startGame(Integer userId, String college, String category) {
        int[] range = getDifficultyRange(userId);
        List<Question> pool = questionRepository.findRandomQuestions(
                college, range[0], range[1], QUESTIONS_PER_ROUND);

        if (pool.size() < QUESTIONS_PER_ROUND) {
            pool.addAll(questionRepository.findRandomQuestions(
                    college, range[0] - 2, range[1] + 2,
                    QUESTIONS_PER_ROUND - pool.size()));
        }

        String sessionId = UUID.randomUUID().toString();
        GameSession session = new GameSession(
                sessionId, userId, college, category, pool,
                TIME_LIMIT_SECONDS, TIMEOUT_EXEMPTIONS);
        activeSessions.put(sessionId, session);
        return session;
    }

    public boolean submitAnswer(String sessionId, int questionIndex, String userAnswer) {
        GameSession session = activeSessions.get(sessionId);
        if (session == null || session.isFinished()) return false;

        Question question = session.getQuestions().get(questionIndex);
        String correct = normalizeAnswer(question.getAnswer());
        String user = normalizeAnswer(userAnswer);
        double score = computeScore(correct, user);
        boolean isCorrect = score > 0;

        session.recordAnswer(questionIndex, score);
        return isCorrect;
    }

    @Transactional
    public GameResultResponse finishGame(String sessionId) {
        GameSession session = activeSessions.remove(sessionId);
        if (session == null) return null;

        int correct = session.getCorrectCount();
        int scoreEarned = calculateScore(session);
        String result = determineResult(session);

        GameHistory history = new GameHistory();
        history.setUserId(session.getUserId());
        history.setCollege(session.getCollege());
        history.setCategory(session.getCategory());
        history.setResult(result);
        history.setCorrectCount(correct);
        history.setTotalTimeSeconds((int) session.getElapsedSeconds());
        history.setScoreEarned(scoreEarned);
        gameHistoryRepository.save(history);

        userService.addScore(session.getUserId(), scoreEarned);

        // Track wrong questions for fully wrong answers (score == 0)
        for (var entry : session.getAnswerScores().entrySet()) {
            if (entry.getValue() <= 0) {
                Question q = session.getQuestions().get(entry.getKey());
                wrongQuestionRepository.findByUserIdAndQuestionId(session.getUserId(), q.getId())
                        .ifPresentOrElse(wq -> {
                            wq.setErrorCount(wq.getErrorCount() + 1);
                            wq.setLastWrongAt(LocalDateTime.now());
                            wrongQuestionRepository.save(wq);
                        }, () -> {
                            WrongQuestion wq = new WrongQuestion();
                            wq.setUserId(session.getUserId());
                            wq.setQuestionId(q.getId());
                            wq.setErrorCount(1);
                            wq.setStatus(0);
                            wq.setCollege(session.getCollege());
                            wq.setLastWrongAt(LocalDateTime.now());
                            wrongQuestionRepository.save(wq);
                        });
            }
        }

        // Build backward-compatible boolean answer map for the response DTO
        Map<Integer, Boolean> answerResults = new HashMap<>();
        for (var entry : session.getAnswerScores().entrySet()) {
            answerResults.put(entry.getKey(), entry.getValue() > 0);
        }
        return new GameResultResponse(result, correct, QUESTIONS_PER_ROUND, scoreEarned,
                answerResults, session.getQuestions());
    }

    private int calculateScore(GameSession session) {
        if (session.hasComboWin()) return 3;
        if (session.getTotalScore() >= 7) return 2;
        if (session.getTotalScore() < 6) return -1;
        return 0;
    }

    private String determineResult(GameSession session) {
        if (session.hasComboWin()) return "win_combo";
        if (session.getTotalScore() >= 7) return "win";
        return "lose";
    }

    private int[] getDifficultyRange(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return switch (user.getRank()) {
            case "青铜" -> new int[]{1, 3};
            case "白银" -> new int[]{2, 4};
            case "黄金" -> new int[]{3, 6};
            case "铂金" -> new int[]{5, 8};
            case "钻石" -> new int[]{7, 9};
            default -> new int[]{8, 10};
        };
    }

    /**
     * Score a multi-choice answer. Single-choice: exact match (0 or 1).
     * Multi-choice: partial credit — hits earn +1, misses penalize -1,
     * normalized by the correct option count, clamped to [0, 1].
     */
    private double computeScore(String correct, String user) {
        if (correct.isEmpty()) return 0;

        Set<Character> correctSet = new HashSet<>();
        for (char c : correct.toCharArray()) correctSet.add(c);
        Set<Character> userSet = new HashSet<>();
        for (char c : user.toCharArray()) userSet.add(c);

        if (correctSet.size() == 1) {
            return correctSet.equals(userSet) ? 1.0 : 0;
        }

        int hits = 0;
        int misses = 0;
        for (char c : userSet) {
            if (correctSet.contains(c)) hits++;
            else misses++;
        }
        double raw = (double) (hits - misses) / correctSet.size();
        return Math.max(0, Math.min(1.0, raw));
    }

    private String normalizeAnswer(String answer) {
        return answer.toUpperCase().replaceAll("[^A-D]", "");
    }

    public static class GameSession {
        private final String sessionId;
        private final Integer userId;
        private final String college;
        private final String category;
        private final List<Question> questions;
        private final Map<Integer, Double> answerScores = new HashMap<>();
        private int correctCount = 0;
        private double totalScore = 0;
        private int comboCount = 0;
        private int maxCombo = 0;
        private int timeoutUsed = 0;
        private final int timeLimitSeconds;
        private final int maxTimeouts;
        private boolean finished = false;
        private final long startTime = System.currentTimeMillis();

        public GameSession(String sessionId, Integer userId, String college, String category,
                           List<Question> questions, int timeLimitSeconds, int maxTimeouts) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.college = college;
            this.category = category;
            this.questions = questions;
            this.timeLimitSeconds = timeLimitSeconds;
            this.maxTimeouts = maxTimeouts;
        }

        public void recordAnswer(int index, double score) {
            answerScores.put(index, score);
            totalScore += score;
            if (score >= 1.0) {
                correctCount++;
                comboCount++;
                maxCombo = Math.max(maxCombo, comboCount);
            } else if (score > 0) {
                comboCount++;
                maxCombo = Math.max(maxCombo, comboCount);
            } else {
                comboCount = 0;
            }
        }

        public boolean hasComboWin() { return maxCombo >= 5; }
        public boolean isFinished() { return finished || answerScores.size() >= questions.size(); }

        public String getSessionId() { return sessionId; }
        public Integer getUserId() { return userId; }
        public String getCollege() { return college; }
        public String getCategory() { return category; }
        public List<Question> getQuestions() { return questions; }
        public Map<Integer, Double> getAnswerScores() { return answerScores; }
        public int getCorrectCount() { return correctCount; }
        public double getTotalScore() { return totalScore; }
        public long getElapsedSeconds() { return (System.currentTimeMillis() - startTime) / 1000; }
        public long getStartTime() { return startTime; }
    }
}
