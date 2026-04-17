package com.app.app.controller;

import com.app.app.dto.PageResponse;
import com.app.app.dto.ProjectDTO;
import com.app.app.model.Project;
import com.app.app.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public PageResponse<ProjectDTO> getAllProjects(Pageable pageable) {
        return projectService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        // 1. The service now returns ProjectDTO directly or throws EntityNotFoundException
        ProjectDTO project = projectService.findById(id);

        // 2. We use .body() to avoid the 'ok' ambiguity entirely
        return ResponseEntity.ok().body(project);
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody Project project) {
        // Service.save now returns ProjectDTO
        return ResponseEntity.ok(projectService.save(project));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody Project updatedProject) {
        // Clean and simple: Service handles the logic
        return ResponseEntity.ok().body(projectService.update(id, updatedProject));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}