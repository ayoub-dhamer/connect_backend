package com.app.app.dto;

import java.util.Set;

public record ProjectDTO(
        Long id,
        String name,
        UserDTO owner,
        Set<UserDTO> participants
) {}
