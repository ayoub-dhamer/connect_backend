package com.app.app.mapper;

import com.app.app.dto.UserDTO;
import com.app.app.model.User;
import java.util.stream.Collectors;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPictureUrl(),
                user.getRoles()
        );
    }
}
