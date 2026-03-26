package com.app.app.mapper;

import com.app.app.dto.ProjectDTO;
import com.app.app.model.Project;
import com.app.app.model.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.email", target = "ownerEmail")
    @Mapping(
            target = "participantIds",
            expression = "java(mapUsersToIds(project.getParticipants()))"
    )
    ProjectDTO toDTO(Project project);

    // Helper method
    default Set<Long> mapUsersToIds(Set<User> users) {
        return users == null
                ? Set.of()
                : users.stream().map(User::getId).collect(Collectors.toSet());
    }
}