package com.app.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "active_calls")
public class ActiveCall {

    @Id
    private String callId;

    @Column(nullable = false)
    private String callerEmail;

    @Column(nullable = false)
    private String receiverEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}