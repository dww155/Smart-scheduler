package com.dww.chat_app.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final CustomJwtDecoder customJwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null || StompCommand.DISCONNECT.equals(command)) {
            return message;
        }

        String token = resolveToken(accessor);
        Authentication authentication = authenticate(token);

        accessor.setUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new MessageDeliveryException("Missing or invalid Authorization header");
        }

        return authorization.substring(BEARER_PREFIX.length());
    }

    private Authentication authenticate(String token) {
        try {
            Jwt jwt = customJwtDecoder.decode(token);
            Authentication authentication = jwtAuthenticationConverter.convert(jwt);

            if (authentication == null) {
                throw new MessageDeliveryException("Invalid token");
            }

            return authentication;
        } catch (RuntimeException e) {
            if (e instanceof MessageDeliveryException) {
                throw e;
            }

            throw new MessageDeliveryException("Invalid token");
        }
    }
}
