package com.platform.controller;

import com.platform.model.*;
import com.platform.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final UserService userService;
    private final CourseService courseService;

    public ProgressController(ProgressService progressService,
                              UserService userService,
                              CourseService courseService) {
        this.progressService = progressService;
        this.userService = userService;
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<UserProgress>> myProgress(
            @AuthenticationPrincipal UserDetails details) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(progressService.getUserProgress(user.getId()));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> courseProgress(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable Long courseId) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(progressService.getCourseProgress(user.getId(), courseId, courseService));
    }

    @PostMapping("/lesson/{lessonId}/start")
    public ResponseEntity<UserProgress> startLesson(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable Long lessonId) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(progressService.markLessonStarted(user.getId(), lessonId));
    }

    @PostMapping("/lesson/{lessonId}/complete")
    public ResponseEntity<UserProgress> completeLesson(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable Long lessonId,
            @RequestParam(defaultValue = "0") Integer score) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(progressService.markLessonCompleted(user.getId(), lessonId, score));
    }

    @GetMapping("/skills")
    public ResponseEntity<List<UserSkillsProgress>> skillTree(
            @AuthenticationPrincipal UserDetails details) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(progressService.getSkillTree(user.getId()));
    }
}
