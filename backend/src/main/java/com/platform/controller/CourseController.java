package com.platform.controller;

import com.platform.model.*;
import com.platform.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/colleges")
    public ResponseEntity<List<College>> getColleges() {
        return ResponseEntity.ok(courseService.getAllColleges());
    }

    @GetMapping("/colleges/{slug}")
    public ResponseEntity<College> getCollege(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getCollege(slug));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getCourses(@RequestParam Long collegeId) {
        return ResponseEntity.ok(courseService.getCourses(collegeId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getCourse(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getFullCourse(slug));
    }

    @GetMapping("/{slug}/chapters")
    public ResponseEntity<List<Chapter>> getChapters(@PathVariable String slug) {
        Course course = courseService.getCourse(slug);
        return ResponseEntity.ok(courseService.getChapters(course.getId()));
    }
}
