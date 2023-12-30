package com.nebulea.ws.config;

import com.nebulea.ws.controllers.WebSocketController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@EnableWebSocket
public class ReactiveWebSocketConfiguration {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter();

        // Add one or more classes annotated with `@ServerEndpoint`.
        serverEndpointExporter.setAnnotatedEndpointClasses(WebSocketController.class);

        return serverEndpointExporter;
    }
}
