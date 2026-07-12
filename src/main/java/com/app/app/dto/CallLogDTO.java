// dto/CallLogDTO.java
package com.app.app.dto;

import java.time.LocalDateTime;

public record CallLogDTO(
        String id,
        String callerEmail,
        String callerName,
        String receiverEmail,
        String receiverName,
        String callType,
        String status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer durationSeconds
) {}