package com.nebulea.ws.ros;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class RosBridgeWebSocketClient {
    @Value("${rosbridge.url}")
    private String rosBridgeUrl;

    public void connectToRosBridge() {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        client.execute(URI.create(rosBridgeUrl), session ->
                session.send(Mono.just(session.textMessage("Your message to ROSBridge")))
                        .thenMany(session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .doOnNext(response -> {
                                    // Handle ROSBridge response
                                    log.info("Received response from ROSBridge: {}", response);
                                })
                        )
                        .then()
        ).subscribe();
    }
}
