package com.platform.repository;

import com.platform.model.LearningReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningReportRepository extends JpaRepository<LearningReport, Long> {
    List<LearningReport> findByUserIdOrderByGeneratedAtDesc(Integer userId);
    List<LearningReport> findByUserIdAndReportType(Integer userId, String reportType);
}
