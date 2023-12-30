package com.nebulea.ws.controllers;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@ServerEndpoint("/event-emitter")
public class WebSocketController {
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) throws IOException {
        // Get session and WebSocket connection
        session.setMaxIdleTimeout(0);
        log.info("Get session and WebSocket connection");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        // Handle new messages
        log.info("Handle new messages -> {}", message);

        session.getBasicRemote().sendText("Your response message");
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        log.info("WebSocket connection closes");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        log.info("Do error handling here");
    }
}
