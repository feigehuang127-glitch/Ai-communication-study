package com.platform.service;

import com.platform.dto.AnswerSubmitRequest;
import com.platform.dto.GameResultResponse;
import com.platform.model.GameHistory;
import com.platform.model.Question;
import com.platform.model.User;
import com.platform.model.WrongQuestion;
import com.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private final QuestionRepository questionRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final WrongQuestionRepository wrongQuestionRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    private final ConcurrentHashMap<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    private static final int QUESTIONS_PER_ROUND = 10;
    private static final int TIME_LIMIT_SECONDS = 10;
    private static final int TIMEOUT_EXEMPTIONS = 2;

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
        String correct = sortAnswer(question.getAnswer());
        String user = sortAnswer(userAnswer);
        boolean isCorrect = correct.equals(user);

        session.recordAnswer(questionIndex, isCorrect, userAnswer);
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

        for (var entry : session.getAnswerResults().entrySet()) {
            if (!entry.getValue()) {
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

        return new GameResultResponse(result, correct, QUESTIONS_PER_ROUND, scoreEarned,
                session.getAnswerResults(), session.getQuestions());
    }

    private int calculateScore(GameSession session) {
        if (session.hasComboWin()) return 3;
        if (session.getCorrectCount() >= 7) return 2;
        if (session.getCorrectCount() < 6) return -1;
        return 0;
    }

    private String determineResult(GameSession session) {
        if (session.hasComboWin()) return "win_combo";
        if (session.getCorrectCount() >= 7) return "win";
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

    private String sortAnswer(String answer) {
        char[] chars = answer.toUpperCase().replaceAll("[^A-D]", "").toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    public static class GameSession {
        private final String sessionId;
        private final Integer userId;
        private final String college;
        private final String category;
        private final List<Question> questions;
        private final Map<Integer, Boolean> answerResults = new HashMap<>();
        private int correctCount = 0;
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

        public void recordAnswer(int index, boolean correct, String answer) {
            answerResults.put(index, correct);
            if (correct) {
                correctCount++;
                comboCount++;
                maxCombo = Math.max(maxCombo, comboCount);
            } else {
                comboCount = 0;
            }
        }

        public boolean hasComboWin() { return maxCombo >= 5; }
        public boolean isFinished() { return finished || answerResults.size() >= questions.size(); }

        public String getSessionId() { return sessionId; }
        public Integer getUserId() { return userId; }
        public String getCollege() { return college; }
        public String getCategory() { return category; }
        public List<Question> getQuestions() { return questions; }
        public Map<Integer, Boolean> getAnswerResults() { return answerResults; }
        public int getCorrectCount() { return correctCount; }
        public long getElapsedSeconds() { return (System.currentTimeMillis() - startTime) / 1000; }
    }
}
