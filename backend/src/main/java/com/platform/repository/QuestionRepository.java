package com.platform.repository;

import com.platform.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByCollegeAndDifficultyBetween(String college, Integer minDiff, Integer maxDiff);

    @Query("SELECT q FROM Question q WHERE q.college = :college " +
           "AND q.difficulty BETWEEN :minDiff AND :maxDiff " +
           "ORDER BY FUNCTION('RAND') LIMIT :limit")
    List<Question> findRandomQuestions(@Param("college") String college,
                                       @Param("minDiff") Integer minDiff,
                                       @Param("maxDiff") Integer maxDiff,
                                       @Param("limit") Integer limit);

    List<Question> findByCollegeAndCategory(String college, String category);

    long countByCollege(String college);

    @Query(value = "SELECT * FROM questions ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomQuestions(@Param("limit") Integer limit);
}
