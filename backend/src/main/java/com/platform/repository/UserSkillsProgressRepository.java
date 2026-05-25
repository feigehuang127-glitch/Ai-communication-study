package com.platform.repository;

import com.platform.model.UserSkillsProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillsProgressRepository extends JpaRepository<UserSkillsProgress, Long> {
    List<UserSkillsProgress> findByUserId(Integer userId);
    Optional<UserSkillsProgress> findByUserIdAndSkillId(Integer userId, String skillId);
}
