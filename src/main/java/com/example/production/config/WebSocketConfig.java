package com.example.production.config;

import com.example.production.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String MESSAGE_TOPIC_PREFIX = "/topic/messages/";
    private static final String USER_MESSAGE_QUEUE = "/user/queue/messages";

    private final JwtDecoder jwtDecoder;
    private final ChatService chatService;

    @Value("${app.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.websocket.broker.relay.enabled:false}")
    private boolean brokerRelayEnabled;

    @Value("${app.websocket.broker.relay.host:localhost}")
    private String brokerRelayHost;

    @Value("${app.websocket.broker.relay.port:61613}")
    private int brokerRelayPort;

    @Value("${app.websocket.broker.relay.client-login:guest}")
    private String brokerRelayClientLogin;

    @Value("${app.websocket.broker.relay.client-passcode:guest}")
    private String brokerRelayClientPasscode;

    @Value("${app.websocket.broker.relay.system-login:guest}")
    private String brokerRelaySystemLogin;

    @Value("${app.websocket.broker.relay.system-passcode:guest}")
    private String brokerRelaySystemPasscode;

    @Value("${app.websocket.broker.relay.virtual-host:}")
    private String brokerRelayVirtualHost;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        if (brokerRelayEnabled) {
            var relay = config.enableStompBrokerRelay("/queue", "/topic")
                    .setRelayHost(brokerRelayHost)
                    .setRelayPort(brokerRelayPort)
                    .setClientLogin(brokerRelayClientLogin)
                    .setClientPasscode(brokerRelayClientPasscode)
                    .setSystemLogin(brokerRelaySystemLogin)
                    .setSystemPasscode(brokerRelaySystemPasscode);

            if (!brokerRelayVirtualHost.isBlank()) {
                relay.setVirtualHost(brokerRelayVirtualHost);
            }
            relay.setUserDestinationBroadcast("/topic/user-destination-broadcast");
            relay.setUserRegistryBroadcast("/topic/user-registry-broadcast");
        } else {
            config.enableSimpleBroker("/queue", "/topic");
        }

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(parseAllowedOrigins())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    authenticateConnect(accessor);
                }
                if (StompCommand.SEND.equals(accessor.getCommand())) {
                    authorizeSend(accessor);
                }
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    authorizeSubscription(accessor);
                }

                return message;
            }
        });
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessageDeliveryException("Missing Authorization header");
        }

        try {
            String token = authHeader.substring(7);
            Jwt jwt = jwtDecoder.decode(token);
            String email = jwt.getSubject();
            accessor.setUser(new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>()));
            log.debug("WebSocket connected: {}", email);
        } catch (MessageDeliveryException e) {
            throw e;
        } catch (Exception e) {
            throw new MessageDeliveryException("WebSocket authentication failed: " + e.getMessage());
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new MessageDeliveryException("Invalid subscription");
        }

        if (USER_MESSAGE_QUEUE.equals(destination)) {
            requireAuthenticated(accessor);
            return;
        }

        if (!destination.startsWith(MESSAGE_TOPIC_PREFIX)) {
            throw new MessageDeliveryException("Subscription is not allowed");
        }

        String email = Optional.ofNullable(accessor.getUser())
                .map(java.security.Principal::getName)
                .orElseThrow(() -> new MessageDeliveryException("Authentication is required"));

        Long conversationId = parseConversationId(destination);
        try {
            chatService.authorizeConversationAccess(conversationId, email);
        } catch (RuntimeException ex) {
            throw new MessageDeliveryException("Not allowed to subscribe to this conversation");
        }
    }

    private void authorizeSend(StompHeaderAccessor accessor) {
        requireAuthenticated(accessor);
        String destination = accessor.getDestination();
        if (!"/app/chat.send".equals(destination) && !"/app/ping".equals(destination)) {
            throw new MessageDeliveryException("Send destination is not allowed");
        }
    }

    private void requireAuthenticated(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new MessageDeliveryException("Authentication is required");
        }
    }

    private Long parseConversationId(String destination) {
        String id = destination.substring(MESSAGE_TOPIC_PREFIX.length());
        if (id.isBlank() || id.contains("/")) {
            throw new MessageDeliveryException("Invalid conversation subscription");
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new MessageDeliveryException("Invalid conversation subscription");
        }
    }

    private String[] parseAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }
}
