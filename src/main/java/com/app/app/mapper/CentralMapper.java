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

    @Mapping(source = "sender.email", target = "senderEmail")
    @Mapping(source = "receiver.email", target = "receiverEmail")
    ChatMessageDTO toDTO(ChatMessage chatMessage);

    @Mapping(source = "caller.email", target = "callerEmail")
    @Mapping(source = "caller.name", target = "callerName")
    @Mapping(source = "receiver.email", target = "receiverEmail")
    @Mapping(source = "receiver.name", target = "receiverName")
    @Mapping(target = "callType", expression = "java(callLog.getCallType() != null ? callLog.getCallType().name() : null)")
    @Mapping(target = "status", expression = "java(callLog.getStatus() != null ? callLog.getStatus().name() : null)")
    CallLogDTO toDTO(CallLog callLog);

    @Mapping(source = "createdBy.email", target = "createdByEmail")
    GroupDTO toDTO(Group group);

    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "sender.email", target = "senderEmail")
    @Mapping(source = "sender.name", target = "senderName")
    GroupMessageDTO toDTO(GroupMessage groupMessage);

    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "startedBy.email", target = "startedByEmail")
    @Mapping(source = "startedBy.name", target = "startedByName")
    @Mapping(target = "callType", expression = "java(session.getCallType() != null ? session.getCallType().name() : null)")
    @Mapping(target = "participants", ignore = true) // populated manually in service, see below
    GroupCallSessionDTO toDTO(GroupCallSession session);

    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.name", target = "name")
    @Mapping(target = "outcome", expression = "java(p.getOutcome() != null ? p.getOutcome().name() : null)")
    ParticipantOutcomeDTO toDTO(GroupCallParticipant p);

    // MapStruct will automatically use this for the Set<User> -> Set<Long> mapping above
    default Set<Long> mapUsersToIds(Set<User> users) {
        if (users == null) return Set.of();
        return users.stream()
                .map(User::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }
}