package com.app.app.controller;

import com.app.app.model.Payment;
import com.app.app.repository.PaymentRepository;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.Optional;
import java.util.stream.Collectors;

//@RestController
//@RequestMapping("/api/payment")
public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final PaymentRepository paymentRepository;

    public WebhookController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            String payload = new BufferedReader(request.getReader()).lines().collect(Collectors.joining());
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

                if (dataObjectDeserializer.getObject().isPresent()) {
                    Session session = (Session) dataObjectDeserializer.getObject().get();
                    String orderId = session.getMetadata().get("orderId");
                    String sessionId = session.getId();

                    Optional<Payment> optionalPayment = paymentRepository.findByOrderId(orderId);
                    if (optionalPayment.isPresent()) {
                        Payment payment = optionalPayment.get();
                        payment.setStatus("COMPLETED");
                        payment.setTransactionId(sessionId);
                        paymentRepository.save(payment);
                    }
                }
            }

            return ResponseEntity.ok("Webhook received");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
}
