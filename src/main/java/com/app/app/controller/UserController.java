package com.app.app.controller;

import com.app.app.dto.UserDTO;
import com.app.app.model.User;
import com.app.app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        // Service throws 404 if email doesn't exist in DB
        return ResponseEntity.ok().body(userService.findByEmail(principal.getName()));
    }

    @GetMapping("/api/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Include subscription status in DTO
        UserDTO dto = centralMapper.toDTO(user);
        dto.setSubscriptionStatus(user.getSubscriptionStatus()); // ACTIVE, NONE, etc.

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public List<UserDTO> getAllUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}