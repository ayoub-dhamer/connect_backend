package com.app.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "project_invitations")
public class ProjectInvitation {
    @Id
    @GeneratedValue
    private Long id;

    private String invitedUserEmail;
    private String token;          // UUID for the invite link
    private InvitationStatus status;

    @ManyToOne
    private Project project;

    @ManyToOne
    private User invitationSender;

    @Future(message = "Expiration date must be in the present or future")
    private LocalDate expirationDate;
}
