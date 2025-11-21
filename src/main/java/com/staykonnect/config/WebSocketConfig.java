package com.staykonnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para mensajes desde el servidor al cliente
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefijo para mensajes desde el cliente al servidor
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefijo para mensajes dirigidos a un usuario específico
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para conexión WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // En producción, especificar dominio exacto
                .withSockJS(); // Fallback a polling si WebSocket no disponible
        
        // Endpoint sin SockJS (WebSocket puro)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
