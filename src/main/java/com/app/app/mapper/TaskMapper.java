package com.app.app.mapper;

import com.app.app.dto.TaskDTO;
import com.app.app.model.Task;

import java.util.stream.Collectors;

public class TaskMapper {
    public static TaskDTO toDTO(Task task) {
        if (task == null) return null;
        return new TaskDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getPriority() != null ? task.getPriority().name() : null,
                task.getStatus() != null ? task.getStatus().name() : null,
                task.isFailed(),
                task.isResolved(),
                task.isExpired(),
                task.getAssignedTeamMembers()
                        .stream()
                        .map(UserMapper::toDTO)
                        .collect(Collectors.toSet()),
                ProjectMapper.toDTO(task.getProject())
        );
    }
}
