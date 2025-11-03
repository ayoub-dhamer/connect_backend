    package com.app.app.model;

    import jakarta.persistence.*;
    import lombok.Data;
    import lombok.ToString;

    import java.util.HashSet;
    import java.util.Set;

    @Entity
    @Data
    @Table(name = "tasks")
    public class Task {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        private String description;

        @Enumerated(EnumType.STRING)
        private TaskPriority priority;

        @Enumerated(EnumType.STRING)
        private TaskStatus status;

        private boolean failed = false;

        private boolean resolved = false;

        private boolean expired = false;

        @ManyToMany
        @JoinTable(
                name = "task_assigned_users",
                joinColumns = @JoinColumn(name = "task_id"),
                inverseJoinColumns = @JoinColumn(name = "user_id")
        )
        @ToString.Exclude
        private Set<User> assignedTeamMembers = new HashSet<>();

        @ManyToOne
        @JoinColumn(name = "project_id")
        private Project project;
    }
