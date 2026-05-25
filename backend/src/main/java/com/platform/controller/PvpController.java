package com.platform.controller;

import com.platform.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pvp")
public class PvpController {

    private final PvpService pvpService;
    private final UserService userService;

    public PvpController(PvpService pvpService, UserService userService) {
        this.pvpService = pvpService;
        this.userService = userService;
    }

    @PostMapping("/queue/join")
    public ResponseEntity<Map<String, Object>> joinQueue(
            @AuthenticationPrincipal UserDetails details) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(pvpService.joinQueue(user.getId()));
    }

    @PostMapping("/queue/leave")
    public ResponseEntity<Map<String, Object>> leaveQueue(
            @AuthenticationPrincipal UserDetails details) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(pvpService.leaveQueue(user.getId()));
    }

    @PostMapping("/result")
    public ResponseEntity<Map<String, Object>> submitResult(
            @AuthenticationPrincipal UserDetails details,
            @RequestBody Map<String, Object> body) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(pvpService.submitPvpResult(
                Long.valueOf(body.get("matchId").toString()),
                user.getId(),
                (int) body.get("correctCount"),
                (int) body.get("totalQuestions")
        ));
    }
}
