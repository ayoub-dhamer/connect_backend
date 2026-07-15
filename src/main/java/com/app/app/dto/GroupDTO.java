// dto/GroupDTO.java
package com.app.app.dto;

import java.util.Set;

public record GroupDTO(
        Long id,
        String name,
        String createdByEmail,
        Set<UserDTO> members
) {}