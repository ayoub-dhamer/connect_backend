package com.app.app.service;

import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveOrUpdateUser(String email, String name, String picture) {
        User user = userRepository.findByEmail(email).orElse(new User());
        user.setEmail(email);
        user.setName(name);
        user.setPictureUrl(picture);

        // Ensure roles is initialized
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        // Add default role if none exist
        if (user.getRoles().isEmpty()) {
            user.getRoles().add("ROLE_USER");
        }

        return userRepository.save(user);
    }



}

