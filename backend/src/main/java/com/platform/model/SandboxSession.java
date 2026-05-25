package com.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sandbox_sessions")
public class SandboxSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "container_id", length = 100)
    private String containerId;

    @Column(name = "template_ref", length = 100)
    private String templateRef;

    @Column(name = "source_course_id")
    private Long sourceCourseId;

    @Column(name = "source_lesson_id")
    private Long sourceLessonId;

    @Column(length = 20)
    private String status = "starting";

    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }
    public String getTemplateRef() { return templateRef; }
    public void setTemplateRef(String templateRef) { this.templateRef = templateRef; }
    public Long getSourceCourseId() { return sourceCourseId; }
    public void setSourceCourseId(Long sourceCourseId) { this.sourceCourseId = sourceCourseId; }
    public Long getSourceLessonId() { return sourceLessonId; }
    public void setSourceLessonId(Long sourceLessonId) { this.sourceLessonId = sourceLessonId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
