package com.app.app.security;

import com.app.app.model.SubscriptionStatus;
import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionGuard {

    private final UserRepository userRepository;

    public SubscriptionGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void checkSubscription(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            throw new AccessDeniedException("Active subscription required");
        }
    }
}