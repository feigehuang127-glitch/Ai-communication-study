package com.platform.repository;

import com.platform.model.PvpMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PvpMatchRepository extends JpaRepository<PvpMatch, Long> {
    List<PvpMatch> findByPlayer1IdOrPlayer2IdOrderByPlayedAtDesc(Integer player1Id, Integer player2Id);
}
