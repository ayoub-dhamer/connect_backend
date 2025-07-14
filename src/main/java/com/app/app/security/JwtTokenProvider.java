package com.app.app.security;

import com.app.app.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expiration:86400000}") long jwtExpirationInMs) {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters.");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtExpirationInMs = jwtExpirationInMs;
    }

    // ✅ Generate Token with only username (for traditional login)
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // ✅ Generate Token with additional user details (for Google OAuth2 login)
    public String generateToken(String email, String name, String picture) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("name", name)
                .claim("picture", picture)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }


    // ✅ Extract username (email or login name) from token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // ✅ Validate Token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            System.err.println("Invalid JWT Token: " + ex.getMessage());
            return false;
        }
    }

    // ✅ Get Authentication from Token (used by JwtAuthenticationFilter)
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("")
                .authorities(role) // e.g., "ROLE_USER"
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

}
