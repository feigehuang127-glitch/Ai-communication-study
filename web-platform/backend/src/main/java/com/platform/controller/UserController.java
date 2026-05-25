package com.platform.controller;

import com.platform.model.User;
import com.platform.security.JwtProvider;
import com.platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtProvider jwtProvider;

    public UserController(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Integer userId = jwtProvider.getUserId(token).intValue();
        User user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Integer userId = jwtProvider.getUserId(token).intValue();
        User user = userService.checkIn(userId);
        return ResponseEntity.ok(Map.of(
                "totalScore", user.getTotalScore(),
                "rank", user.getRank(),
                "lastCheckinDate", user.getLastCheckinDate().toString()
        ));
    }
}
