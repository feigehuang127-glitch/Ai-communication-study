package com.platform.service;

import com.platform.model.*;
import com.platform.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PvpService {

    private final PvpMatchRepository matchRepo;
    private final QuestionRepository questionRepo;
    private final RankingService rankingService;
    private final ConcurrentLinkedQueue<Integer> matchQueue = new ConcurrentLinkedQueue<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PvpService(PvpMatchRepository matchRepo,
                      QuestionRepository questionRepo,
                      RankingService rankingService) {
        this.matchRepo = matchRepo;
        this.questionRepo = questionRepo;
        this.rankingService = rankingService;
    }

    public Map<String, Object> joinQueue(Integer userId) {
        if (matchQueue.contains(userId)) {
            return Map.of("status", "already_queued");
        }
        Integer opponent = matchQueue.poll();
        if (opponent != null && !opponent.equals(userId)) {
            return createMatch(userId, opponent);
        }
        matchQueue.add(userId);
        return Map.of("status", "queued");
    }

    public Map<String, Object> leaveQueue(Integer userId) {
        matchQueue.remove(userId);
        return Map.of("status", "left");
    }

    private Map<String, Object> createMatch(Integer player1Id, Integer player2Id) {
        List<Question> allQuestions = questionRepo.findAll();
        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.subList(0, Math.min(10, allQuestions.size()));

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

        if (match.getWinnerId() == null) {
            match.setWinnerId(userId);
            try {
                match.setScores(objectMapper.writeValueAsString(
                        Map.of(userId.toString(), Map.of("correct", correctCount, "score", score))));
            } catch (Exception e) {
                match.setScores("{}");
            }
            matchRepo.save(match);
            rankingService.updateScore(userId.longValue(), score);
            return Map.of("status", "completed", "winnerId", userId, "score", score);
        }

        rankingService.updateScore(userId.longValue(), score);
        return Map.of("status", "completed", "winnerId", match.getWinnerId(), "score", score);
    }
}
