package com.platform.controller;

import com.platform.model.*;
import com.platform.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final LearningReportService reportService;
    private final UserService userService;

    public ReportController(LearningReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<LearningReport>> myReports(
            @AuthenticationPrincipal UserDetails details) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(reportService.getUserReports(user.getId()));
    }

    @PostMapping("/generate")
    public ResponseEntity<LearningReport> generateReport(
            @AuthenticationPrincipal UserDetails details,
            @RequestBody Map<String, Object> body) {
        var user = userService.findByUsername(details.getUsername());
        Long courseId = Long.valueOf(body.get("courseId").toString());
        String reportType = (String) body.getOrDefault("reportType", "weekly");
        return ResponseEntity.ok(reportService.generateReport(user.getId(), courseId, reportType));
    }

    @GetMapping("/skill-tree")
    public ResponseEntity<Map<String, Object>> skillTree(
            @AuthenticationPrincipal UserDetails details) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(reportService.getSkillTreeVisualization(user.getId()));
    }
}
