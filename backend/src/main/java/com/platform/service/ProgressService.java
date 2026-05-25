package com.platform.service;

import com.platform.model.*;
import com.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ProgressService {

    private final UserProgressRepository userProgressRepo;
    private final UserSkillsProgressRepository skillsRepo;
    private final CourseService courseService;

    public ProgressService(UserProgressRepository userProgressRepo,
                           UserSkillsProgressRepository skillsRepo,
                           CourseService courseService) {
        this.userProgressRepo = userProgressRepo;
        this.skillsRepo = skillsRepo;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<UserProgress> getUserProgress(Integer userId) {
        return userProgressRepo.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<UserProgress> getLessonProgress(Integer userId, Long lessonId) {
        return userProgressRepo.findByUserIdAndLessonId(userId, lessonId);
    }

    public UserProgress markLessonStarted(Integer userId, Long lessonId) {
        UserProgress p = userProgressRepo.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    UserProgress np = new UserProgress();
                    np.setUserId(userId);
                    np.setLessonId(lessonId);
                    return np;
                });
        p.setStatus("in_progress");
        return userProgressRepo.save(p);
    }

    public UserProgress markLessonCompleted(Integer userId, Long lessonId, Integer score) {
        UserProgress p = userProgressRepo.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    UserProgress np = new UserProgress();
                    np.setUserId(userId);
                    np.setLessonId(lessonId);
                    return np;
                });
        p.setStatus("completed");
        if (score != null) p.setScore(score);
        p.setCompletedAt(LocalDateTime.now());
        return userProgressRepo.save(p);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCourseProgress(Integer userId, Long courseId) {
        List<Chapter> chapters = courseService.getChapters(courseId);
        int totalLessons = 0;
        int completedLessons = 0;
        List<Map<String, Object>> chapterProgress = new ArrayList<>();

        for (Chapter ch : chapters) {
            List<Lesson> lessons = courseService.getLessons(ch.getId());
            int chCompleted = 0;
            for (Lesson lesson : lessons) {
                totalLessons++;
                Optional<UserProgress> p = getLessonProgress(userId, lesson.getId());
                if (p.isPresent() && "completed".equals(p.get().getStatus())) {
                    completedLessons++;
                    chCompleted++;
                }
            }
            Map<String, Object> chMap = new HashMap<>();
            chMap.put("chapterId", ch.getId());
            chMap.put("title", ch.getTitle());
            chMap.put("totalLessons", lessons.size());
            chMap.put("completedLessons", chCompleted);
            chapterProgress.add(chMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("courseId", courseId);
        result.put("totalLessons", totalLessons);
        result.put("completedLessons", completedLessons);
        result.put("chapters", chapterProgress);
        return result;
    }

    @Transactional(readOnly = true)
    public List<UserSkillsProgress> getSkillTree(Integer userId) {
        return skillsRepo.findByUserId(userId);
    }

    public UserSkillsProgress unlockSkill(Integer userId, String skillId) {
        UserSkillsProgress sp = skillsRepo.findByUserIdAndSkillId(userId, skillId)
                .orElseGet(() -> {
                    UserSkillsProgress nsp = new UserSkillsProgress();
                    nsp.setUserId(userId);
                    nsp.setSkillId(skillId);
                    return nsp;
                });
        sp.setStatus(1);
        return skillsRepo.save(sp);
    }

    public UserSkillsProgress masterSkill(Integer userId, String skillId) {
        UserSkillsProgress sp = skillsRepo.findByUserIdAndSkillId(userId, skillId)
                .orElseGet(() -> {
                    UserSkillsProgress nsp = new UserSkillsProgress();
                    nsp.setUserId(userId);
                    nsp.setSkillId(skillId);
                    return nsp;
                });
        sp.setStatus(3);
        sp.setUpdatedAt(LocalDateTime.now());
        return skillsRepo.save(sp);
    }
}
