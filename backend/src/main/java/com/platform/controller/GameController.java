package com.platform.controller;

import com.platform.dto.AnswerSubmitRequest;
import com.platform.dto.GameResultResponse;
import com.platform.dto.GameStartRequest;
import com.platform.security.JwtProvider;
import com.platform.service.GameService;
import com.platform.service.GameService.GameSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final JwtProvider jwtProvider;

    public GameController(GameService gameService, JwtProvider jwtProvider) {
        this.gameService = gameService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(
            @RequestBody GameStartRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Integer userId = jwtProvider.getUserId(token);
        GameSession session = gameService.startGame(userId, request.getCollege(), request.getCategory());
        return ResponseEntity.ok(Map.of(
                "sessionId", session.getSessionId(),
                "questions", session.getQuestions(),
                "timeLimit", 10
        ));
    }

    @PostMapping("/answer")
    public ResponseEntity<Map<String, Object>> submitAnswer(
            @RequestBody AnswerSubmitRequest request) {
        boolean correct = gameService.submitAnswer(
                request.getSessionId(), request.getQuestionIndex(), request.getAnswer());
        return ResponseEntity.ok(Map.of("correct", correct));
    }

    @PostMapping("/finish")
    public ResponseEntity<GameResultResponse> finishGame(@RequestBody Map<String, String> body) {
        GameResultResponse result = gameService.finishGame(body.get("sessionId"));
        return ResponseEntity.ok(result);
    }
}
