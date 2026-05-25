package com.platform.controller;

import com.platform.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final RankingService rankingService;

    public LeaderboardController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> topPlayers(
            @RequestParam(defaultValue = "20") int count) {
        return ResponseEntity.ok(rankingService.getTopPlayers(count));
    }
}
