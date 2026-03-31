package com.app.app.controller;

import com.app.app.service.UserService;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class StripeWebhookController {

    private final UserService userService;

    public StripeWebhookController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();
            String email = session.getCustomerEmail();

            User user = userRepository.findByEmail(email).orElseThrow();
            user.setSubscriptionStatus("ACTIVE");
            userRepository.save(user);
        }

        return ResponseEntity.ok("");
    }
}
