package com.app.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true)
    private String email;    // Use email as primary key / username

    @NotBlank(message = "Name is required")
    private String name;

    private String pictureUrl;

    private String preferredLanguage = "en";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    private Set<String> roles = new HashSet<>();

    @Column(name = "subscription_status")
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.UNPAID;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    // 👇 Inverse side of Project.participants
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // Exclude collections from hash/equals
    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    // 👇 Inverse side of Task.assignedTeamMembers
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // Exclude collections from hash/equals
    @ManyToMany(mappedBy = "assignedTeamMembers")
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();
}
