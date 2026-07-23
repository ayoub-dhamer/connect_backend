// dto/GroupActivityDTO.java
package com.app.app.dto;

import java.time.LocalDateTime;

public record GroupActivityDTO(
        Long id,
        String type,
        String actorName,
        String targetName,
        String detail,
        LocalDateTime timestamp
) {}