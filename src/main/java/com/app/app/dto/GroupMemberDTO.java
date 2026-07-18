// dto/GroupMemberDTO.java
package com.app.app.dto;

public record GroupMemberDTO(
        Long id,
        String email,
        String name,
        String pictureUrl,
        String role
) {}