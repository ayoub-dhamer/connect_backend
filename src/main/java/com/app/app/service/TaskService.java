package com.app.app.service;

import com.app.app.dto.TaskDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.Task;
import com.app.app.repository.TaskRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CentralMapper mapper; // Clean injection of CentralMapper

    public TaskService(TaskRepository taskRepository, CentralMapper mapper) {
        this.taskRepository = taskRepository;
        this.mapper = mapper;
    }

    public List<TaskDTO> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TaskDTO> findById(Long id) {
        return taskRepository.findById(id).map(mapper::toDTO);
    }

    // Accepts Entity from Controller, saves, and returns DTO to Frontend
    public TaskDTO save(Task task) {
        return mapper.toDTO(taskRepository.save(task));
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    public Optional<TaskDTO> update(Long id, Task updatedTask) {
        return taskRepository.findById(id).map(existing -> {
            existing.setName(updatedTask.getName());
            existing.setDescription(updatedTask.getDescription());
            existing.setPriority(updatedTask.getPriority());
            existing.setStatus(updatedTask.getStatus());
            existing.setAssignedTeamMembers(updatedTask.getAssignedTeamMembers());
            existing.setProject(updatedTask.getProject());
            return mapper.toDTO(taskRepository.save(existing));
        });
    }
}