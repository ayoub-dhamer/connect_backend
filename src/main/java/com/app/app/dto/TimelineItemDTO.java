// dto/TimelineItemDTO.java
package com.app.app.dto;

import java.time.LocalDateTime;

public record TimelineItemDTO(
        String kind,              // "message" | "call"
        LocalDateTime timestamp,
        ChatMessageDTO message,   // populated when kind == "message"
        CallLogDTO call           // populated when kind == "call"
) {}