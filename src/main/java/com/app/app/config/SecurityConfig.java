package com.app.app.config;

import com.app.app.security.JwtAuthenticationFilter;
import com.app.app.security.JwtTokenProvider;
import com.app.app.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            UserService userService,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:4200"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowCredentials(true); // Required for HttpOnly Cookies
                    return config;
                }))
                // CSRF protection for Cookie-based Auth
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )

                // authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/api/payments/**",
                                "/ws/**"
                        ).permitAll()//check this because i used this to bypass the check only so delete this and add a security check in angular
                        .anyRequest().authenticated()
                )

                // stateless sessions (JWT-based)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 login handler
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            String email = oauthUser.getAttribute("email");
                            String name = oauthUser.getAttribute("name");
                            String picture = oauthUser.getAttribute("picture");

                            // Save or update user
                            var user = userService.saveOrUpdateUser(email, name, picture);

                            // Get roles
                            var roles = user.roles();

                            // ✅ Generate JWT
                            String jwt = jwtTokenProvider.generateToken(email, name, picture, roles);

                            // ✅ Set JWT in HttpOnly cookie (safer than URL)
                            Cookie cookie = new Cookie("jwt", jwt);
                            cookie.setHttpOnly(true);
                            cookie.setSecure(false); // true in production (HTTPS)
                            cookie.setPath("/");
                            cookie.setMaxAge(24 * 60 * 60); // 1 day

                            response.addCookie(cookie);

                            // ✅ Redirect to frontend WITHOUT token in URL
                            response.sendRedirect("http://localhost:4200/login-success");
                        })
                );

        return http.build();
    }
}
