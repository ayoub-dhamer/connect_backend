package com.app.app.mapper;

import com.app.app.dto.*;
import com.app.app.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CentralMapper {

    // User Mappings
    UserDTO toDTO(User user);

    // Project Mappings
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.email", target = "ownerEmail")
    @Mapping(target = "participantIds", expression = "java(mapUsersToIds(project.getParticipants()))")
    ProjectDTO toDTO(Project project);

    // Task Mappings
    @Mapping(target = "priority", expression = "java(task.getPriority() != null ? task.getPriority().name() : null)")
    @Mapping(target = "status", expression = "java(task.getStatus() != null ? task.getStatus().name() : null)")
    TaskDTO toDTO(Task task);

    default Set<Long> mapUsersToIds(Set<User> users) {
        return users == null ? Set.of() : users.stream().map(User::getId).collect(Collectors.toSet());
    }
}