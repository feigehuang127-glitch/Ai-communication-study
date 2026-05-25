package com.platform.repository;

import com.platform.model.SandboxSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SandboxSessionRepository extends JpaRepository<SandboxSession, Long> {
    List<SandboxSession> findByUserIdAndStatus(Integer userId, String status);
    Optional<SandboxSession> findByContainerId(String containerId);
}
