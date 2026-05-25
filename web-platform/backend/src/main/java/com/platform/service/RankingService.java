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
        try {
            redis.opsForZSet().incrementScore(LEADERBOARD_KEY, userId.toString(), score);
        } catch (Exception e) {
            // Redis failure - silently ignore, ranking will be stale
        }
    }

    public Long getRank(Long userId) {
        try {
            Long rank = redis.opsForZSet().reverseRank(LEADERBOARD_KEY, userId.toString());
            return rank == null ? -1 : rank + 1;
        } catch (Exception e) {
            return -1L;
        }
    }

    public Double getScore(Long userId) {
        try {
            return redis.opsForZSet().score(LEADERBOARD_KEY, userId.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<Map<String, Object>> getTopPlayers(int count) {
        try {
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
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
