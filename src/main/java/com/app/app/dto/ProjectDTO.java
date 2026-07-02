package com.app.app.dto;

import com.app.app.model.ProjectStatus;
import com.app.app.model.User;

import java.time.LocalDate;
import java.util.Set;

public record ProjectDTO(
        Long id,
        String name,
        String description,
        LocalDate expirationDate,
        ProjectStatus status,
        User owner,
        Set<UserDTO> participants,
        Set<TaskDTO> tasks
) {}
