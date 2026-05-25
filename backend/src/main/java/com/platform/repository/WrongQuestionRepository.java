package com.platform.repository;

import com.platform.model.WrongQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WrongQuestionRepository extends JpaRepository<WrongQuestion, Long> {
    List<WrongQuestion> findByUserIdOrderByLastWrongAtDesc(Integer userId);
    Optional<WrongQuestion> findByUserIdAndQuestionId(Integer userId, Integer questionId);
    long countByUserIdAndStatus(Integer userId, Integer status);
    List<WrongQuestion> findByUserIdAndStatus(Integer userId, Integer status);
}
