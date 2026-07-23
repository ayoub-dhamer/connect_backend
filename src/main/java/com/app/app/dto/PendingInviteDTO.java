// dto/PendingInviteDTO.java
package com.app.app.dto;

public record PendingInviteDTO(
        String callId,
        String roomId,
        Long groupId,
        String groupName,
        String callType,
        String callerEmail,
        String callerName
) {}