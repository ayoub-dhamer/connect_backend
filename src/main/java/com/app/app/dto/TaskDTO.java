package com.app.app.dto;

import java.util.Set;

public record TaskDTO(
        Long id,
        String name,
        String description,
        String priority,
        String status,
        boolean failed,
        boolean resolved,
        boolean expired,
        Set<UserDTO> assignedTeamMembers,
        ProjectDTO project
) {}
