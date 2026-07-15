// dto/GroupCallSessionDTO.java
package com.app.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GroupCallSessionDTO(
        String callId,
        Long groupId,
        String startedByEmail,
        String startedByName,
        String callType,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<ParticipantOutcomeDTO> participants
) {}