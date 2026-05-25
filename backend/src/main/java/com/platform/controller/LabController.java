package com.platform.controller;

import com.platform.model.*;
import com.platform.repository.*;
import com.platform.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    private final LabProjectRepository labProjectRepo;
    private final UserService userService;

    public LabController(LabProjectRepository labProjectRepo, UserService userService) {
        this.labProjectRepo = labProjectRepo;
        this.userService = userService;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<LabProject>> listProjects(@AuthenticationPrincipal UserDetails details) {
        var user = userService.findByUsername(details.getUsername());
        return ResponseEntity.ok(labProjectRepo.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    @PostMapping("/projects")
    public ResponseEntity<LabProject> saveProject(@AuthenticationPrincipal UserDetails details,
                                                   @RequestBody Map<String, Object> body) {
        var user = userService.findByUsername(details.getUsername());
        LabProject project = new LabProject();
        project.setUserId(user.getId());
        project.setType((String) body.getOrDefault("type", "agent"));
        project.setTitle((String) body.getOrDefault("title", "Untitled"));
        project.setSnapshot((String) body.getOrDefault("snapshot", "{}"));
        return ResponseEntity.ok(labProjectRepo.save(project));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long id) {
        labProjectRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
