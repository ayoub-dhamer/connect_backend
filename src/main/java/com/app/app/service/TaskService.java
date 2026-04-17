package com.app.app.service;

import com.app.app.dto.PageResponse;
import com.app.app.dto.TaskDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.Task;
import com.app.app.repository.TaskRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final CentralMapper centralMapper;

    public TaskService(TaskRepository taskRepository, CentralMapper centralMapper) {
        this.taskRepository = taskRepository;
        this.centralMapper = centralMapper;
    }

    public PageResponse<TaskDTO> findAll(Pageable pageable) {
        return PageResponse.of(
                taskRepository.findAll(pageable).map(centralMapper::toDTO)
        );
    }

    public TaskDTO findById(Long id) {
        return taskRepository.findById(id)
                .map(centralMapper::toDTO)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Task not found with id: " + id));
    }

    public TaskDTO save(Task task) {
        return centralMapper.toDTO(taskRepository.save(task));
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Cannot delete: Task " + id + " not found");
        }
        taskRepository.deleteById(id);
    }

    public TaskDTO update(Long id, Task updatedTask) {
        return taskRepository.findById(id).map(existing -> {
            existing.setName(updatedTask.getName());
            existing.setDescription(updatedTask.getDescription());
            existing.setPriority(updatedTask.getPriority());
            existing.setStatus(updatedTask.getStatus());
            existing.setFailed(updatedTask.isFailed());
            existing.setResolved(updatedTask.isResolved());
            existing.setExpired(updatedTask.isExpired());
            existing.setAssignedTeamMembers(updatedTask.getAssignedTeamMembers());
            existing.setProject(updatedTask.getProject());
            return centralMapper.toDTO(taskRepository.save(existing));
        }).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cannot update: Task " + id + " not found"));
    }
}