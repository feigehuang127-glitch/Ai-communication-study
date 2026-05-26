package com.platform.controller;

import com.platform.model.WrongQuestion;
import com.platform.repository.WrongQuestionRepository;
import com.platform.security.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrongbook")
public class WrongBookController {

    private final WrongQuestionRepository wrongQuestionRepository;
    private final JwtProvider jwtProvider;

    public WrongBookController(WrongQuestionRepository wrongQuestionRepository, JwtProvider jwtProvider) {
        this.wrongQuestionRepository = wrongQuestionRepository;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping
    public ResponseEntity<List<WrongQuestion>> getWrongQuestions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int status) {
        String token = authHeader.substring(7);
        Integer userId = jwtProvider.getUserId(token);
        return ResponseEntity.ok(wrongQuestionRepository.findByUserIdAndStatus(userId, status));
    }
}
