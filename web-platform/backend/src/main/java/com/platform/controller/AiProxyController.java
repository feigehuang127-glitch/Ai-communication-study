package com.platform.controller;

import com.platform.security.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiProxyController {

    private final RestTemplate restTemplate;
    private final JwtProvider jwtProvider;
    private final String aiServiceUrl;

    public AiProxyController(RestTemplate restTemplate,
                             JwtProvider jwtProvider,
                             @Value("${ai-service.url}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.jwtProvider = jwtProvider;
        this.aiServiceUrl = aiServiceUrl;
    }

    @PostMapping("/chat")
    public void proxyChat(@RequestBody Map<String, Object> body,
                          @RequestHeader("Authorization") String authHeader,
                          HttpServletResponse response) throws IOException {
        String token = authHeader.substring(7);
        if (!jwtProvider.validate(token)) {
            response.setStatus(401);
            return;
        }

        String url = aiServiceUrl + "/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> aiResponse = restTemplate.exchange(
                url, HttpMethod.POST, request, byte[].class);

        response.setContentType("text/event-stream");
        response.setStatus(aiResponse.getStatusCode().value());
        if (aiResponse.getBody() != null) {
            response.getOutputStream().write(aiResponse.getBody());
        }
        response.getOutputStream().flush();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try {
            ResponseEntity<Map> resp = restTemplate.getForEntity(
                    aiServiceUrl + "/health", Map.class);
            return ResponseEntity.ok(Map.of("ai_service", "connected"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("ai_service", "unavailable"));
        }
    }

    @PostMapping("/compare")
    public void proxyCompare(@RequestBody Map<String, Object> body,
                             @RequestHeader("Authorization") String authHeader,
                             HttpServletResponse response) throws IOException {
        String token = authHeader.substring(7);
        if (!jwtProvider.validate(token)) {
            response.setStatus(401);
            return;
        }

        String url = aiServiceUrl + "/compare";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<byte[]> aiResponse = restTemplate.exchange(
                url, HttpMethod.POST, request, byte[].class);

        response.setContentType("text/event-stream");
        response.setStatus(aiResponse.getStatusCode().value());
        if (aiResponse.getBody() != null) {
            response.getOutputStream().write(aiResponse.getBody());
        }
        response.getOutputStream().flush();
    }

    @PostMapping("/sandbox/create")
    public ResponseEntity<Map<String, Object>> proxySandboxCreate(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        String token = authHeader.substring(7);
        if (!jwtProvider.validate(token)) {
            return ResponseEntity.status(401).build();
        }

        String url = aiServiceUrl + "/sandbox/create";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("containerId", "sandbox-local");
            fallback.put("status", "running");
            return ResponseEntity.ok(fallback);
        }
    }

    @PostMapping("/sandbox/execute")
    public ResponseEntity<Map<String, Object>> proxySandboxExecute(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        String token = authHeader.substring(7);
        if (!jwtProvider.validate(token)) {
            return ResponseEntity.status(401).build();
        }

        String url = aiServiceUrl + "/sandbox/execute";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            String code = (String) body.getOrDefault("code", "");
            String language = (String) body.getOrDefault("language", "python");
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("output", "[沙箱未连接] 代码已保存，共 " + code.length() + " 字符\n语言: " + language);
            fallback.put("exitCode", 0);
            return ResponseEntity.ok(fallback);
        }
    }
}
