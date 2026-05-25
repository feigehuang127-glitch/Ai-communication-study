package com.platform.repository;

import com.platform.model.LabProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabProjectRepository extends JpaRepository<LabProject, Long> {
    List<LabProject> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<LabProject> findByIsPublishedTrue();
}
