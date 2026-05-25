package com.platform.repository;

import com.platform.model.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    List<GameHistory> findByUserIdOrderByPlayedAtDesc(Integer userId);
    long countByUserId(Integer userId);
}
