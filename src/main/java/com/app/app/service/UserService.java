package com.app.app.service;

import com.app.app.dto.UserDTO;
import com.app.app.mapper.CentralMapper; // 1. Import CentralMapper
import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CentralMapper mapper; // 2. Add mapper dependency

    public UserService(UserRepository userRepository, CentralMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public UserDTO saveOrUpdateUser(String email, String name, String picture) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);

        user.setEmail(email);
        user.setName(name);
        user.setPictureUrl(picture);

        if (user.getPreferredLanguage() == null) {
            user.setPreferredLanguage("en");
        }

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        if (user.getRoles().isEmpty()) {
            user.getRoles().add("ROLE_USER");
        }

        User savedUser = userRepository.save(user);

        // 3. Use the central mapper instead of a private helper
        return mapper.toDTO(savedUser);
    }

    public List<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> findById(Long id) {
        return userRepository.findById(id).map(mapper::toDTO);
    }

    public Optional<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email).map(mapper::toDTO);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

}