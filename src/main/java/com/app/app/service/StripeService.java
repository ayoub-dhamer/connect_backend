package com.app.app.service;

import com.app.app.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value; // ✅
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public StripeService() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey; // ✅ runs after injection
    }

    public String createCheckoutSession(User user, String planId) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomerEmail(user.getEmail())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(planId) // Stripe Price ID
                                .setQuantity(1L)
                                .build()
                )
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl("http://localhost:4200/payment/success")
                .setCancelUrl("http://localhost:4200/payment/cancel")
                .build();

        Session session = Session.create(params);
        return session.getId();
    }
}
