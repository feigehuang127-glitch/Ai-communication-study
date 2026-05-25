package com.platform.service;

import com.platform.model.*;
import com.platform.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseService {

    private final CollegeRepository collegeRepo;
    private final CourseRepository courseRepo;
    private final ChapterRepository chapterRepo;
    private final LessonRepository lessonRepo;

    public CourseService(CollegeRepository collegeRepo, CourseRepository courseRepo,
                         ChapterRepository chapterRepo, LessonRepository lessonRepo) {
        this.collegeRepo = collegeRepo;
        this.courseRepo = courseRepo;
        this.chapterRepo = chapterRepo;
        this.lessonRepo = lessonRepo;
    }

    public List<College> getAllColleges() {
        return collegeRepo.findAll();
    }

    public College getCollege(String slug) {
        return collegeRepo.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("College not found: " + slug));
    }

    public List<Course> getCourses(Long collegeId) {
        return courseRepo.findByCollegeIdOrderByOrderAsc(collegeId);
    }

    public Course getCourse(String slug) {
        return courseRepo.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Course not found: " + slug));
    }

    public List<Chapter> getChapters(Long courseId) {
        return chapterRepo.findByCourseIdOrderByOrderAsc(courseId);
    }

    public List<Lesson> getLessons(Long chapterId) {
        return lessonRepo.findByChapterIdOrderByOrderAsc(chapterId);
    }

    public Map<String, Object> getFullCourse(String slug) {
        Course course = getCourse(slug);
        List<Chapter> chapters = getChapters(course.getId());
        List<Map<String, Object>> chapterData = new ArrayList<>();
        for (Chapter ch : chapters) {
            List<Lesson> lessons = getLessons(ch.getId());
            chapterData.add(Map.of(
                    "chapter", ch,
                    "lessons", lessons
            ));
        }
        return Map.of(
                "course", course,
                "chapters", chapterData
        );
    }

    public String getCourseContentForRag(String slug) {
        Course course = getCourse(slug);
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(course.getTitle()).append("\n\n");
        sb.append(course.getDescription()).append("\n\n");
        for (Chapter ch : getChapters(course.getId())) {
            sb.append("## ").append(ch.getTitle()).append("\n\n");
            for (Lesson lesson : getLessons(ch.getId())) {
                sb.append("### ").append(lesson.getTitle()).append("\n");
                sb.append(lesson.getContent()).append("\n\n");
            }
        }
        return sb.toString();
    }
}
