package com.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.model.BehaviorEvent;
import com.platform.repository.BehaviorEventRepository;
import com.platform.security.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/behavior")
public class BehaviorEventController {

    private static final Logger log = LoggerFactory.getLogger(BehaviorEventController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final BehaviorEventRepository repo;
    private final JwtProvider jwtProvider;

    public BehaviorEventController(BehaviorEventRepository repo, JwtProvider jwtProvider) {
        this.repo = repo;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> ingest(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody List<Map<String, Object>> events) {

        Integer userId = extractUserId(auth);
        int saved = 0;

        for (Map<String, Object> e : events) {
            try {
                BehaviorEvent be = new BehaviorEvent();
                be.setUserId(userId);
                be.setSessionId((String) e.getOrDefault("sessionId", ""));
                be.setEventType((String) e.getOrDefault("type", "unknown"));
                be.setCreatedAt(LocalDateTime.now());
                Object data = e.get("data");
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    dataMap.put("url", e.getOrDefault("url", ""));
                    be.setContext(objectMapper.writeValueAsString(dataMap));
                }
                repo.save(be);
                saved++;
            } catch (Exception ex) {
                // skip malformed event, don't fail the batch
            }
        }

        log.debug("Ingested {} behavior events for user {}", saved, userId);
        return ResponseEntity.ok(Map.of("received", events.size(), "saved", saved));
    }

    private Integer extractUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return 0;
        try {
            return jwtProvider.getUserId(auth.substring(7));
        } catch (Exception e) {
            return 0;
        }
    }
}
