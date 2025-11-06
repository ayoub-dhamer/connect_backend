package com.app.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true)
    private String email;    // Use email as primary key / username

    private String name;

    private String pictureUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    private Set<String> roles = new HashSet<>();

    // 👇 Inverse side of Project.participants
    @ToString.Exclude
    @ManyToMany(mappedBy = "participants")
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    // 👇 Inverse side of Task.assignedTeamMembers
    @ToString.Exclude
    @ManyToMany(mappedBy = "assignedTeamMembers")
    @JsonIgnore
    private Set<Task> tasks = new HashSet<>();
}
