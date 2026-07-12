package com.app.app.config;

import com.app.app.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // ── RabbitMQ relay config — inert while the simple broker (below) is
    // active. Uncomment configureMessageBroker's relay block + these fields
    // when actually deploying multiple backend instances behind a load
    // balancer. Requires RabbitMQ running with the STOMP plugin enabled
    // (see docker-compose.yml) and the spring.rabbitmq.* properties in
    // application.properties.
    //
    // @Value("${spring.rabbitmq.host}")
    // private String relayHost;
    //
    // @Value("${spring.rabbitmq.stomp.port}")
    // private int relayPort;
    //
    // @Value("${spring.rabbitmq.username}")
    // private String relayLogin;
    //
    // @Value("${spring.rabbitmq.password}")
    // private String relayPasscode;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200")
                .addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider))
                .setHandshakeHandler(new UserHandshakeHandler())
                .withSockJS()
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js")
                .setSessionCookieNeeded(true);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");

        // ── Current: in-memory simple broker ──────────────────────────
        // Fine for a single backend instance. Does NOT work across multiple
        // instances behind a load balancer — a user connected to instance A
        // cannot receive messages sent by a user connected to instance B.
        registry.enableSimpleBroker("/topic", "/queue");

        // ── Multi-instance alternative: RabbitMQ STOMP relay ──────────
        // Swap the line above for this block (and delete/comment out the
        // enableSimpleBroker call) once deploying multiple instances.
        // Also uncomment the @Value fields above.
        //
        // registry.enableStompBrokerRelay("/topic", "/queue")
        //         .setRelayHost(relayHost)
        //         .setRelayPort(relayPort)
        //         .setClientLogin(relayLogin)
        //         .setClientPasscode(relayPasscode)
        //         .setSystemLogin(relayLogin)
        //         .setSystemPasscode(relayPasscode);

        registry.setUserDestinationPrefix("/user");
    }
}