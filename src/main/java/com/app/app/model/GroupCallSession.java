// GroupCallSession.java
package com.app.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "group_call_sessions")
public class GroupCallSession {

    @Id
    private String callId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_by_id", nullable = false)
    private User startedBy;

    @Enumerated(EnumType.STRING)
    private CallType callType;

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime endedAt;
}