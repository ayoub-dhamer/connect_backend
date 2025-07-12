package com.app.app.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthResponse {
    private String jwt;
    private String email;
    private String name;
    private String picture;
    private String phone;
}
