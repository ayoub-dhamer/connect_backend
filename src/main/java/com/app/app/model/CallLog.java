// CallLog.java
package com.app.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "call_logs")
public class CallLog {

    @Id
    private String id; // the callId generated on the frontend (crypto.randomUUID())

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    private CallType callType;

    @Enumerated(EnumType.STRING)
    private CallStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
}