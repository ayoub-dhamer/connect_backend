package com.app.app.mapper;

import com.app.app.dto.*;
import com.app.app.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CentralMapper {

    UserDTO toDTO(User user);

    ProjectDTO toDTO(Project project);

    @Mapping(target = "priority", expression = "java(task.getPriority() != null ? task.getPriority().name() : null)")
    @Mapping(target = "status", expression = "java(task.getStatus() != null ? task.getStatus().name() : null)")
    TaskDTO toDTO(Task task);

    // MapStruct will automatically use this for the Set<User> -> Set<Long> mapping above
    default Set<Long> mapUsersToIds(Set<User> users) {
        if (users == null) return Set.of();
        return users.stream()
                .map(User::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }
}