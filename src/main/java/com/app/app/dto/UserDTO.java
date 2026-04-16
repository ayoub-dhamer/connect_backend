package com.app.app.dto;

import com.app.app.model.SubscriptionStatus;

import java.util.Set;

public record UserDTO(
        Long id,
        String email,
        String name,
        String pictureUrl,
        String preferredLanguage,
        Set<String> roles,

        // ✅ ADD THIS
        SubscriptionStatus subscriptionStatus
) {
}