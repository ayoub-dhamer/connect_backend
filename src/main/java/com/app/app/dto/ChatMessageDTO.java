package com.app.app.dto;

import java.time.LocalDateTime;

public record ChatMessageDTO(
        Long id,
        String senderEmail,
        String receiverEmail,
        String content,
        LocalDateTime timestamp
) {}