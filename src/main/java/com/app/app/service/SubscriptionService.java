package com.app.app.service;

import com.app.app.model.SubscriptionStatus;
import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {


    private final UserRepository userRepository;

    public SubscriptionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void assertActiveSubscription(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new SecurityException("Unauthenticated");
        }

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getSubscriptionStatus() != SubscriptionStatus.ACTIVE){
            throw new SecurityException("Subscription required");
        }
    }
}