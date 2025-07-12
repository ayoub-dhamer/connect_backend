package com.app.app.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;    // Use email as primary key / username

    private String name;
    private String pictureUrl;
    private String password; // can be null for OAuth users
}
