package com.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pvp_matches")
public class PvpMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player1_id", nullable = false)
    private Integer player1Id;

    @Column(name = "player2_id", nullable = false)
    private Integer player2Id;

    @Column(name = "winner_id")
    private Integer winnerId;

    @Column(nullable = false, columnDefinition = "JSON")
    private String questions;

    @Column(columnDefinition = "JSON")
    private String scores;

    @Column(name = "played_at")
    private LocalDateTime playedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(Integer player1Id) { this.player1Id = player1Id; }
    public Integer getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(Integer player2Id) { this.player2Id = player2Id; }
    public Integer getWinnerId() { return winnerId; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }
    public String getQuestions() { return questions; }
    public void setQuestions(String questions) { this.questions = questions; }
    public String getScores() { return scores; }
    public void setScores(String scores) { this.scores = scores; }
    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}
