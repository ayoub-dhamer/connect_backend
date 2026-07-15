package com.app.app.model;

public record GroupCallSignal(
        String type,
        String callId,
        String roomId,
        String callType,
        String callerEmail,
        String callerName,
        Long groupId,
        String groupName,
        String respondentEmail,
        String startedAt
) {}