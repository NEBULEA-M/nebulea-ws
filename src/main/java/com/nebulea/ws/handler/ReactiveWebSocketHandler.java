package com.nebulea.ws.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@Component("ReactiveWebSocketHandler")
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private final String rosBridgeUrl;

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        // Connect to ROS Bridge
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        return client.execute(URI.create(rosBridgeUrl), rosSession -> {
            // Forward message from client to ROS Bridge
            Mono<Void> forwardToRosBridge = webSocketSession.receive()
                    .doOnNext(webSocketMessage -> {
                        String clientMessage = webSocketMessage.getPayloadAsText();
                        // Handle the received message here
                        log.info("Received message from client: {}", clientMessage);

                        // Forward message to ROS Bridge
                        rosSession.send(Mono.just(rosSession.textMessage(clientMessage)))
                                .subscribe();
                    })
                    .then();

            // Forward message from ROS Bridge to the client
            Mono<Void> forwardToWebSocket = rosSession.receive()
                    .concatMap(response -> {
                        String responseBody = response.getPayloadAsText();
                        log.info("Received response from ROS Bridge: {}", responseBody);

                        // Send ROS Bridge response to the client
                        return webSocketSession.send(Mono.just(webSocketSession.textMessage(responseBody)));
                    })
                    .then();

            return Mono.zip(forwardToRosBridge, forwardToWebSocket)
                    .then();
        });
    }
}
