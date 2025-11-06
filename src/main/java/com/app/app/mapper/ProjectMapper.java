package com.app.app.mapper;

import com.app.app.dto.ProjectDTO;
import com.app.app.model.Project;

import java.util.stream.Collectors;

public class ProjectMapper {
    public static ProjectDTO toDTO(Project project) {
        if (project == null) return null;
        return new ProjectDTO(
                project.getId(),
                project.getName(),
                UserMapper.toDTO(project.getOwner()),
                project.getParticipants()
                        .stream()
                        .map(UserMapper::toDTO)
                        .collect(Collectors.toSet())
        );
    }
}
