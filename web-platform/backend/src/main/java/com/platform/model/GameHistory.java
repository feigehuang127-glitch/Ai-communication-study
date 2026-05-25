package com.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_history")
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(length = 20)
    private String college = "comm";

    @Column(length = 20)
    private String mode = "pve";

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String result;

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "total_time_seconds")
    private Integer totalTimeSeconds = 0;

    @Column(name = "score_earned")
    private Integer scoreEarned = 0;

    @Column(name = "played_at")
    private LocalDateTime playedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public Integer getTotalTimeSeconds() { return totalTimeSeconds; }
    public void setTotalTimeSeconds(Integer totalTimeSeconds) { this.totalTimeSeconds = totalTimeSeconds; }
    public Integer getScoreEarned() { return scoreEarned; }
    public void setScoreEarned(Integer scoreEarned) { this.scoreEarned = scoreEarned; }
    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}
