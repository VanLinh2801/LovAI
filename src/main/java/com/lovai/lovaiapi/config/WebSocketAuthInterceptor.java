package com.lovai.lovaiapi.config;

import com.lovai.lovaiapi.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                List<String> authHeaders = accessor.getNativeHeader("Authorization");
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    String authHeader = authHeaders.get(0);
                    
                    if (authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        
                        if (jwtUtil.validateToken(token)) {
                            String email = jwtUtil.getEmailFromToken(token);
                            UUID userId = jwtUtil.getUserIdFromToken(token);
                            
                            if (email != null && userId != null) {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                                
                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                                
                                WebSocketPrincipal principal = new WebSocketPrincipal(userId, email, authentication);
                                accessor.setUser(principal);
                                
                                logger.info("WebSocket authentication successful for user: {} ({})", email, userId);
                            } else {
                                logger.warn("Invalid JWT token - missing user info");
                                return null; // Reject connection
                            }
                        } else {
                            logger.warn("Invalid JWT token");
                            return null; // Reject connection
                        }
                    } else {
                        logger.warn("Invalid Authorization header format");
                        return null; // Reject connection
                    }
                } else {
                    logger.warn("No Authorization header found");
                    return null; // Reject connection
                }
            } catch (Exception e) {
                logger.error("Error during WebSocket authentication: {}", e.getMessage(), e);
                return null; // Reject connection
            }
        }
        
        return message;
    }

    public static class WebSocketPrincipal implements Principal {
        private final UUID userId;
        private final String email;
        private final Authentication authentication;

        public WebSocketPrincipal(UUID userId, String email, Authentication authentication) {
            this.userId = userId;
            this.email = email;
            this.authentication = authentication;
        }

        @Override
        public String getName() {
            return email;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public Authentication getAuthentication() {
            return authentication;
        }
    }
}
