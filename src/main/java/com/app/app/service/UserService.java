package com.app.app.service;

import com.app.app.dto.UserDTO;
import com.app.app.mapper.CentralMapper;
import com.app.app.model.SubscriptionStatus;
import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CentralMapper centralMapper;

    public UserService(UserRepository userRepository, CentralMapper centralMapper) {
        this.userRepository = userRepository;
        this.centralMapper = centralMapper;
    }

    public UserDTO saveOrUpdateUser(String email, String name, String picture) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setRoles(new HashSet<>(List.of("ROLE_USER")));
            newUser.setSubscriptionStatus(SubscriptionStatus.UNPAID); // default
            return newUser;
        });

        user.setName(name);
        user.setPictureUrl(picture);

        if (user.getPreferredLanguage() == null) {
            user.setPreferredLanguage("en");
        }

        return centralMapper.toDTO(userRepository.save(user));
    }

    public void updateSubscription(String email, String stripeCustomerId, String stripeSubscriptionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setStripeCustomerId(stripeCustomerId);
        user.setStripeSubscriptionId(stripeSubscriptionId);
        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);

        userRepository.save(user);
    }

    public void cancelSubscription(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setSubscriptionStatus(SubscriptionStatus.CANCELED);
        userRepository.save(user);
    }

    public List<UserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).stream()
                .map(centralMapper::toDTO)
                .toList();
    }

    public boolean hasActiveSubscription(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getSubscriptionStatus() == SubscriptionStatus.ACTIVE)
                .orElse(false);
    }

    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(centralMapper::toDTO)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found with id: " + id));
    }

    public UserDTO findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(centralMapper::toDTO)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found with email: " + email));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Cannot delete: User " + id + " not found");
        }
        userRepository.deleteById(id);
    }
}