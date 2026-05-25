package com.platform.controller;

import com.platform.model.User;
import com.platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal UserDetails details) {
        User user = userService.findByUsername(details.getUsername());
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/checkin")
    public ResponseEntity<Map<String, Object>> checkIn(@AuthenticationPrincipal UserDetails details) {
        User user = userService.findByUsername(details.getUsername());
        user = userService.checkIn(user.getId());
        user.setPasswordHash(null);
        return ResponseEntity.ok(Map.of(
                "totalScore", user.getTotalScore(),
                "rank", user.getRank(),
                "lastCheckinDate", user.getLastCheckinDate().toString()
        ));
    }
}
