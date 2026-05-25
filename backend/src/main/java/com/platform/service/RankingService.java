package com.platform.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RankingService {

    private static final String LEADERBOARD_KEY = "leaderboard:rank";
    private final RedisTemplate<String, String> redis;

    public RankingService(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    public void updateScore(Long userId, int score) {
        redis.opsForZSet().incrementScore(LEADERBOARD_KEY, userId.toString(), score);
    }

    public Long getRank(Long userId) {
        Long rank = redis.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
        return rank == null ? -1 : rank + 1;
    }

    public Double getScore(Long userId) {
        return redis.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
    }

    public List<Map<String, Object>> getTopPlayers(int count) {
        Set<ZSetOperations.TypedTuple<String>> top = redis.opsForZSet()
                .reverseRangeWithScores(LEADERBOARD_KEY, 0, count - 1);
        List<Map<String, Object>> result = new ArrayList<>();
        if (top != null) {
            int rank = 1;
            for (var tuple : top) {
                result.add(Map.of(
                        "rank", rank++,
                        "userId", Long.parseLong(tuple.getValue()),
                        "score", tuple.getScore()
                ));
            }
        }
        return result;
    }
}
