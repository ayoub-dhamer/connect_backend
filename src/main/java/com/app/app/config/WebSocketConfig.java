package com.app.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Allowed origins for dev (Angular)
    private static final String[] ALLOWED = {"http://localhost:4200"};

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP endpoint Angular will connect to (SockJS fallback)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(ALLOWED)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients send messages to /app/...
        registry.setApplicationDestinationPrefixes("/app");
        // Broker will broadcast to /topic/...
        registry.enableSimpleBroker("/topic");
        // Optionally enable user destination prefix for personal queues
        registry.setUserDestinationPrefix("/user");
    }
}
