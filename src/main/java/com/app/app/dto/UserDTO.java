package com.app.app.dto;

import java.util.Set;

public record UserDTO(
        Long id,
        String email,
        String name,
        String pictureUrl,
        String preferredLanguage,
        Set<String> roles
) {}