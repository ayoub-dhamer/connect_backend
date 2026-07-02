package com.app.app.service;

import com.app.app.dto.CreateProjectRequest;
import com.app.app.dto.PageResponse;
import com.app.app.dto.ProjectDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.*;
import com.app.app.repository.ProjectRepository;
import com.app.app.repository.TaskRepository;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable; // Ensure this import is here
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CentralMapper centralMapper; // 1. Inject the new CentralMapper

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, TaskRepository taskRepository, CentralMapper centralMapper) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.centralMapper = centralMapper;
    }

    // 2. Return a List of DTOs and handle Pagination mapping
    public PageResponse<ProjectDTO> findAll(Pageable pageable) {
        return PageResponse.of(
                projectRepository.findAll(pageable).map(centralMapper::toDTO)
        );
    }

    // 3. Keep the Optional but map the internal value to DTO
    public ProjectDTO findById(Long id) {
        return projectRepository.findById(id)
                .map(centralMapper::toDTO)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Project not found with id: " + id));
    }

    // 4. Accept the Entity, save, and return the DTO
    public ProjectDTO save(Project project) {
        Project savedProject = projectRepository.save(project);
        return centralMapper.toDTO(savedProject);
    }

    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Cannot delete: Project " + id + " not found");
        }
        // If this project has tasks, the DB throws DataIntegrityViolationException,
        // which your GlobalExceptionHandler already handles!
        projectRepository.deleteById(id);
    }

    public ProjectDTO update(Long id, Project updatedProject) {
        return projectRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedProject.getName());
                    existing.setOwner(updatedProject.getOwner());
                    existing.setParticipants(updatedProject.getParticipants());
                    existing.setStatus(updatedProject.getStatus());
                    return centralMapper.toDTO(projectRepository.save(existing));
                })
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cannot update: Project " + id + " not found"));
    }

    // ProjectService.java
    public ProjectDTO createFromRequest(CreateProjectRequest request, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStatus(ProjectStatus.valueOf(request.status()));
        project.setOwner(owner);

        if (request.participantIds() != null && !request.participantIds().isEmpty()) {
            Set<User> participants = new HashSet<>(userRepository.findAllById(request.participantIds()));
            project.setParticipants(participants);
        }

        Project saved = projectRepository.save(project);

        if (request.tasks() != null) {
            for (var t : request.tasks()) {
                Task task = new Task();
                task.setName(t.name());
                task.setPriority(TaskPriority.valueOf(t.priority()));
                task.setStatus(TaskStatus.valueOf(t.status()));
                task.setProject(saved);
                if (t.assignedMemberIds() != null && !t.assignedMemberIds().isEmpty()) {
                    task.setAssignedTeamMembers(new HashSet<>(userRepository.findAllById(t.assignedMemberIds())));
                }
                taskRepository.save(task);
            }
        }

        // pendingInviteEmails: hook into ProjectInvitation creation here if you want that flow wired up too

        return centralMapper.toDTO(saved);
    }
}