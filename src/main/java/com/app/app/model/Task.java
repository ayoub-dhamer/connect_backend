    package com.app.app.model;

    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
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

        @NotBlank(message = "Task name is required")
        @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
        private String name;

        @Size(max = 1000, message = "Description is too long")
        private String description;

        @NotNull(message = "Priority is required")
        @Enumerated(EnumType.STRING)
        private TaskPriority priority;

        @NotNull(message = "Status is required")
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
        @JsonIgnoreProperties("tasks")
        private Set<User> assignedTeamMembers = new HashSet<>();

        @NotNull(message = "Task must belong to a project")
        @ManyToOne
        @JoinColumn(name = "project_id")
        private Project project;
    }
