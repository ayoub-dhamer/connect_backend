package com.app.app.config;

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

import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // disable csrf for APIs
                .csrf(csrf -> csrf.disable())

                // authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers("/**").permitAll()//check this because i used this to bypass the check only so delete this and add a security check in angular
                        .anyRequest().authenticated()
                )

                // stateless sessions (JWT-based)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // OAuth2 login handler
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            String email = oauthUser.getAttribute("email");
                            String name = oauthUser.getAttribute("name");
                            String picture = oauthUser.getAttribute("picture");
                           /* var roles = oauthUser.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .collect(Collectors.toList());*/

                            var user = userService.saveOrUpdateUser(email, name, picture);

                            var roles = user.getRoles();

                            // generate JWT
                            String jwt = jwtTokenProvider.generateToken(email, name, picture, roles);

                            // redirect frontend with JWT
                            response.sendRedirect("http://localhost:4200/login-success?token=" + jwt);
                        })
                );

        return http.build();
    }
}
