package com.app.app.controller;

import com.app.app.model.ProcessedStripeEvent;
import com.app.app.model.User;
import com.app.app.repository.ProcessedStripeEventRepository;
import com.app.app.service.UserService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Value("${stripe.secret.key}")
    private String endpointSecret;

    private final UserService userService;
    private final ProcessedStripeEventRepository processedEventRepository;

    public StripeWebhookController(
            UserService userService,
            ProcessedStripeEventRepository processedEventRepository) {
        this.userService = userService;
        this.processedEventRepository = processedEventRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        // ✅ Idempotency check — skip already-processed events
        if (processedEventRepository.existsById(event.getId())) {
            return ResponseEntity.ok("already processed");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow();

            String email = session.getCustomerEmail();
            userService.activateSubscription(email);
        }

        // ✅ Mark event as processed
        processedEventRepository.save(new ProcessedStripeEvent(event.getId()));

        return ResponseEntity.ok("received");
    }
}