// dto/ActiveGroupCallDTO.java
package com.app.app.dto;

import java.time.LocalDateTime;

// ActiveGroupCallDTO.java — add roomId
public record ActiveGroupCallDTO(
        String callId,
        String roomId,
        Long groupId,
        String callType,
        LocalDateTime startedAt
) {}