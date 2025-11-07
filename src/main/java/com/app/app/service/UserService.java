package com.app.app.service;

import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
        user.setPreferredLanguage("en");

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


    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(String.valueOf(id));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void delete(Long id) {
        userRepository.deleteById(String.valueOf(id));
    }
}

