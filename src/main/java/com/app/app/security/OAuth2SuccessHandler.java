package com.app.app.security;

import com.app.app.model.User;
import com.app.app.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtTokenProvider jwtTokenProvider,
                                UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Save or update user in DB
        userRepository.findById(email).ifPresentOrElse(user -> {
            user.setName(name);
            user.setPictureUrl(picture);
            userRepository.save(user);
        }, () -> {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPictureUrl(picture);
            userRepository.save(user);
        });

        // Generate JWT token with email as subject + extra claims if needed
        String token = jwtTokenProvider.generateToken(email, name, picture);

        Map<String, String> tokenResponse = new HashMap<>();
        tokenResponse.put("token", token);
        tokenResponse.put("email", email);
        tokenResponse.put("name", name);
        tokenResponse.put("picture", picture);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), tokenResponse);
    }
}
