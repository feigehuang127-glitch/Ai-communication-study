package com.platform.service;

import com.platform.model.*;
import com.platform.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PvpService {

    private final PvpMatchRepository matchRepo;
    private final QuestionRepository questionRepo;
    private final RankingService rankingService;
    private final ConcurrentLinkedQueue<Integer> matchQueue = new ConcurrentLinkedQueue<>();
    private final Set<Integer> queuedSet = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PvpService(PvpMatchRepository matchRepo,
                      QuestionRepository questionRepo,
                      RankingService rankingService) {
        this.matchRepo = matchRepo;
        this.questionRepo = questionRepo;
        this.rankingService = rankingService;
    }

    public Map<String, Object> joinQueue(Integer userId) {
        // O(1) lookup via ConcurrentHashMap.newKeySet() instead of O(N) queue scan
        if (!queuedSet.add(userId)) {
            return Map.of("status", "already_queued");
        }
        Integer opponent = matchQueue.poll();
        if (opponent != null) {
            queuedSet.remove(opponent);
            if (!opponent.equals(userId)) {
                queuedSet.remove(userId);
                return createMatch(userId, opponent);
            }
        }
        matchQueue.add(userId);
        return Map.of("status", "queued");
    }

    public Map<String, Object> leaveQueue(Integer userId) {
        matchQueue.remove(userId);
        queuedSet.remove(userId);
        return Map.of("status", "left");
    }

    @Transactional
    private Map<String, Object> createMatch(Integer player1Id, Integer player2Id) {
        // Use native SQL RAND() with LIMIT — works across all colleges (ENUM 'ai','comm')
        List<Question> selected = questionRepo.findRandomQuestions(10);

        PvpMatch match = new PvpMatch();
        match.setPlayer1Id(player1Id);
        match.setPlayer2Id(player2Id);
        try {
            match.setQuestions(objectMapper.writeValueAsString(
                    selected.stream().map(Question::getId).toList()));
        } catch (Exception e) {
            match.setQuestions("[]");
        }
        match = matchRepo.save(match);

        return Map.of(
                "status", "matched",
                "matchId", match.getId(),
                "player1Id", player1Id,
                "player2Id", player2Id,
                "questions", selected
        );
    }

    @Transactional
    public Map<String, Object> submitPvpResult(Long matchId, Integer userId,
                                                int correctCount, int totalQuestions) {
        PvpMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        int score = correctCount * 10;

        // Load existing scores
        Map<String, Map<String, Integer>> scores;
        try {
            String existing = match.getScores();
            scores = (existing == null || existing.isEmpty()) ? new HashMap<>()
                    : objectMapper.readValue(existing, new TypeReference<Map<String, Map<String, Integer>>>() {});
        } catch (Exception e) {
            scores = new HashMap<>();
        }

        // Record this player's result
        scores.put(userId.toString(), Map.of("correct", correctCount, "score", score));

        // If both players have submitted, determine winner
        if (scores.size() >= 2) {
            Integer winnerId = null;
            int highestCorrect = -1;
            for (var entry : scores.entrySet()) {
                int c = entry.getValue().get("correct");
                if (c > highestCorrect) {
                    highestCorrect = c;
                    winnerId = Integer.parseInt(entry.getKey());
                }
            }
            match.setWinnerId(winnerId);
        }

        try {
            match.setScores(objectMapper.writeValueAsString(scores));
        } catch (Exception e) {
            match.setScores("{}");
        }
        matchRepo.save(match);

        // Always update Redis ranking
        try {
            rankingService.updateScore(userId.longValue(), score);
        } catch (Exception e) {
            // Redis failure shouldn't block match result
        }

        return Map.of("status", scores.size() >= 2 ? "completed" : "waiting_for_opponent",
                "score", score);
    }
}
