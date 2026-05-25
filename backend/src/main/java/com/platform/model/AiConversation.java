package com.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_conversations")
public class AiConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "context_page", length = 100)
    private String contextPage;

    @Column(name = "context_lesson_id")
    private Long contextLessonId;

    @Column(name = "model_used", length = 50)
    private String modelUsed;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getContextPage() { return contextPage; }
    public void setContextPage(String contextPage) { this.contextPage = contextPage; }
    public Long getContextLessonId() { return contextLessonId; }
    public void setContextLessonId(Long contextLessonId) { this.contextLessonId = contextLessonId; }
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
