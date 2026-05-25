package com.platform.repository;

import com.platform.model.BehaviorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BehaviorEventRepository extends JpaRepository<BehaviorEvent, Long> {
    List<BehaviorEvent> findByUserIdAndSessionId(Integer userId, String sessionId);
}
