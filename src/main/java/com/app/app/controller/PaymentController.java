package com.app.app.controller;

import com.app.app.model.PaymentRequest;
import com.app.app.service.SubscriptionService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PreAuthorize("hasRole('ROLE_PREMIUM')")  // or check subscription via a custom annotation
    @GetMapping("/premium-data")
    public ResponseEntity<String> getPremiumData() {
        return ResponseEntity.ok("This is premium content!");
    }

    // PaymentController.java — replace createCheckoutSession
    @PostMapping("/create-checkout-session")
    public Map<String, Object> createCheckoutSession(
            @RequestBody @Valid PaymentRequest request) throws StripeException {

        Stripe.apiKey = stripeSecretKey;

        long amountInCents = Math.round(request.getAmount() * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/payment-cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(request.getProductName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return Map.of("id", session.getId(), "url", session.getUrl());
    }
}
