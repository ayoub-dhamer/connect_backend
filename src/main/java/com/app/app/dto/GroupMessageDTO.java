// dto/GroupMessageDTO.java
package com.app.app.dto;

import java.time.LocalDateTime;

public record GroupMessageDTO(
        Long id,
        Long groupId,
        String senderEmail,
        String senderName,
        String content,
        LocalDateTime timestamp
) {}