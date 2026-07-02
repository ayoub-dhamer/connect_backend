package com.app.app.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(
        String name,
        String description,
        String status,
        LocalDate deadline,
        List<Long> participantIds,
        List<String> pendingInviteEmails,
        List<CreateTaskRequest> tasks
) {
    public record CreateTaskRequest(
            String name,
            String priority,
            String status,
            List<Long> assignedMemberIds
    ) {}
}