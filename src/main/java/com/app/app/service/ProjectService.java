package com.app.app.service;

import com.app.app.dto.ProjectDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.Project;
import com.app.app.repository.ProjectRepository;
import org.springframework.data.domain.Pageable; // Ensure this import is here
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CentralMapper centralMapper; // 1. Inject the new CentralMapper

    public ProjectService(ProjectRepository projectRepository, CentralMapper centralMapper) {
        this.projectRepository = projectRepository;
        this.centralMapper = centralMapper;
    }

    // 2. Return a List of DTOs and handle Pagination mapping
    public List<ProjectDTO> findAll(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .stream()
                .map(centralMapper::toDTO)
                .collect(Collectors.toList());
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
}