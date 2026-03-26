package com.app.app.dto;

import java.util.Set;

public record ProjectDTO(
        Long id,
        String name,
        Long ownerId,
        String ownerEmail,
        Set<Long> participantIds
) {}
