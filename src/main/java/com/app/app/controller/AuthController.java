package com.app.app.controller;

import com.app.app.model.User;
import com.app.app.security.JwtTokenProvider;
import com.app.app.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")  // adjust your Angular frontend URL here
public class AuthController {

    private final String googleClientId;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public AuthController(@Value("${google.client.id}") String googleClientId,
                          JwtTokenProvider jwtTokenProvider,
                          UserService userService) {
        this.googleClientId = googleClientId;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("idToken");
        if (idTokenString == null || idTokenString.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID token is required"));
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            User user = userService.saveOrUpdateUser(email, name, picture);
            String jwt = jwtTokenProvider.generateToken(email, name, picture);

            // Optional: Authenticate the user in Spring Security context
            Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return ResponseEntity.ok(Map.of(
                    "jwt", jwt,
                    "email", user.getEmail(),
                    "name", user.getName(),
                    "picture", user.getPictureUrl() != null ? user.getPictureUrl() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Token verification failed: " + e.getMessage()));
        }
    }
}
