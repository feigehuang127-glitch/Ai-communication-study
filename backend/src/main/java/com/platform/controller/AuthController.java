package com.platform.controller;

import com.platform.dto.LoginRequest;
import com.platform.dto.LoginResponse;
import com.platform.model.User;
import com.platform.security.JwtProvider;
import com.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtProvider jwtProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authManager,
                          JwtProvider jwtProvider,
                          UserService userService) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userService.findByUsername(request.getUsername());
        String token = jwtProvider.generateToken(user.getUsername(), user.getId(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(
                token, user.getUsername(), user.getRole(), user.getTotalScore(), user.getRank()));
    }
}
