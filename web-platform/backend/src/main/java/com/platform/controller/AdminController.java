package com.platform.controller;

import com.platform.model.*;
import com.platform.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CollegeRepository collegeRepo;
    private final CourseRepository courseRepo;
    private final ChapterRepository chapterRepo;
    private final LessonRepository lessonRepo;
    private final QuestionRepository questionRepo;

    public AdminController(CollegeRepository collegeRepo, CourseRepository courseRepo,
                           ChapterRepository chapterRepo, LessonRepository lessonRepo,
                           QuestionRepository questionRepo) {
        this.collegeRepo = collegeRepo;
        this.courseRepo = courseRepo;
        this.chapterRepo = chapterRepo;
        this.lessonRepo = lessonRepo;
        this.questionRepo = questionRepo;
    }

    // === Colleges ===
    @GetMapping("/colleges")
    public ResponseEntity<List<College>> listColleges() {
        return ResponseEntity.ok(collegeRepo.findAll());
    }

    @PostMapping("/colleges")
    public ResponseEntity<College> createCollege(@RequestBody College college) {
        return ResponseEntity.ok(collegeRepo.save(college));
    }

    // === Courses ===
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> listCourses() {
        return ResponseEntity.ok(courseRepo.findAll());
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseRepo.save(course));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        return ResponseEntity.ok(courseRepo.save(course));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Map<String, String>> deleteCourse(@PathVariable Long id) {
        courseRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }

    // === Chapters ===
    @GetMapping("/courses/{courseId}/chapters")
    public ResponseEntity<List<Chapter>> listChapters(@PathVariable Long courseId) {
        return ResponseEntity.ok(chapterRepo.findByCourseIdOrderByOrderAsc(courseId));
    }

    @PostMapping("/chapters")
    public ResponseEntity<Chapter> createChapter(@RequestBody Chapter chapter) {
        return ResponseEntity.ok(chapterRepo.save(chapter));
    }

    @PutMapping("/chapters/{id}")
    public ResponseEntity<Chapter> updateChapter(@PathVariable Long id, @RequestBody Chapter chapter) {
        chapter.setId(id);
        return ResponseEntity.ok(chapterRepo.save(chapter));
    }

    // === Lessons ===
    @GetMapping("/chapters/{chapterId}/lessons")
    public ResponseEntity<List<Lesson>> listLessons(@PathVariable Long chapterId) {
        return ResponseEntity.ok(lessonRepo.findByChapterIdOrderByOrderAsc(chapterId));
    }

    @PostMapping("/lessons")
    public ResponseEntity<Lesson> createLesson(@RequestBody Lesson lesson) {
        return ResponseEntity.ok(lessonRepo.save(lesson));
    }

    @PutMapping("/lessons/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long id, @RequestBody Lesson lesson) {
        lesson.setId(id);
        return ResponseEntity.ok(lessonRepo.save(lesson));
    }

    // === Questions ===
    @GetMapping("/questions")
    public ResponseEntity<List<Question>> listQuestions() {
        return ResponseEntity.ok(questionRepo.findAll());
    }

    @PostMapping("/questions")
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) {
        return ResponseEntity.ok(questionRepo.save(question));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Integer id, @RequestBody Question question) {
        question.setId(id);
        return ResponseEntity.ok(questionRepo.save(question));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Map<String, String>> deleteQuestion(@PathVariable Integer id) {
        questionRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
