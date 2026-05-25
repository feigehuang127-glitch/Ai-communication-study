package com.platform.repository;

import com.platform.model.UserWrongQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWrongQuestionRepository extends JpaRepository<UserWrongQuestion, Long> {
    List<UserWrongQuestion> findByUserIdAndStatus(Integer userId, Integer status);
    List<UserWrongQuestion> findByUserIdOrderByErrorCountDesc(Integer userId);
}
