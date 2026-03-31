package com.app.app.controller;

import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import com.app.app.service.StripeService;
import com.stripe.exception.StripeException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {
    private final UserRepository userRepository;
    private final StripeService stripeService;

    public CheckoutController(UserRepository userRepository, StripeService stripeService) {
        this.userRepository = userRepository;
        this.stripeService = stripeService;
    }

    @PostMapping("/checkout")
    public Map<String, String> createCheckout(@RequestBody Map<String, String> body, Authentication auth) throws StripeException {
        String planId = body.get("planId");
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Create Stripe Checkout session
        String sessionId = stripeService.createCheckoutSession(user, planId);
        return Map.of("id", sessionId);
    }
}
