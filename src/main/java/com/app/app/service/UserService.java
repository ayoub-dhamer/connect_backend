package com.app.app.service;

import com.app.app.dto.UserDTO;
import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO saveOrUpdateUser(String email, String name, String picture) {
        // 1. Fetch existing user or create a NEW Entity (not a DTO)
        User user = userRepository.findByEmail(email).orElseGet(User::new);

        user.setEmail(email);
        user.setName(name);
        user.setPictureUrl(picture);

        // Only set default if it's a new user or language is missing
        if (user.getPreferredLanguage() == null) {
            user.setPreferredLanguage("en");
        }

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        if (user.getRoles().isEmpty()) {
            user.getRoles().add("ROLE_USER");
        }

        // 2. Save the entity
        User savedUser = userRepository.save(user);

        // 3. Map the saved entity back to a DTO
        return mapToDTO(savedUser);
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> findById(Long id) {
        return userRepository.findById(id).map(this::mapToDTO);
    }

    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::mapToDTO);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    // Helper method to convert Entity -> DTO
    private UserDTO mapToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPictureUrl(),
                user.getPreferredLanguage(),
                user.getRoles()
        );
    }
}