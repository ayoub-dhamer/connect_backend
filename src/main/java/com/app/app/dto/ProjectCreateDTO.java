package com.app.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record ProjectCreateDTO(
        @NotBlank String name,
        @NotNull Long ownerId,
        Set<Long> participantIds
) {}