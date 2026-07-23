package com.app.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "group_activities")
public class GroupActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    private GroupActivityType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor; // who performed the action (null-safe if system-generated)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    private User target; // who the action was about (e.g. the added/removed member)

    private String detail; // e.g. old/new group name for RENAMED

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}