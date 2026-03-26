package com.app.app.service;

import com.app.app.dto.TaskDTO;
import com.app.app.model.Task;
import com.app.app.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskDTO> findAll() {
        return taskRepository.findAll();
    }

    public Optional<TaskDTO> findById(Long id) {
        return taskRepository.findById(id);
    }

    public TaskDTO save(TaskDTO task) {
        return taskRepository.save(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
