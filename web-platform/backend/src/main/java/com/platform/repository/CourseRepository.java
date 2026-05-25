package com.platform.repository;

import com.platform.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCollegeIdOrderByOrderAsc(Long collegeId);
    Optional<Course> findBySlug(String slug);
}
