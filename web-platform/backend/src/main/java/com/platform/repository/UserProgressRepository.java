package com.platform.repository;

import com.platform.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(Integer userId);
    Optional<UserProgress> findByUserIdAndLessonId(Integer userId, Long lessonId);
    List<UserProgress> findByUserIdAndStatus(Integer userId, String status);
}
