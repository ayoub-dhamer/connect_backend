package com.app.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_stripe_events")
public class ProcessedStripeEvent {

    @Id
    private String eventId; // Stripe event ID is already unique

    private LocalDateTime processedAt;

    public ProcessedStripeEvent() {}

    public ProcessedStripeEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
}