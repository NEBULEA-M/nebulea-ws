package com.nebulea.ws.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

public class AuthorizationHeaderPayload {

    public static Mono<String> extract(ServerWebExchange serverWebExchange) {
        // Try to extract the access token from the query parameter
        return Mono.justOrEmpty(Optional.ofNullable(serverWebExchange.getRequest()
                        .getQueryParams()
                        .getFirst(ServerHttpBearerAuthenticationConverter.AUTHORIZATION_PARAM))
                        .map(s -> {
                            if (!StringUtils.isBlank(s)) {
                                return ServerHttpBearerAuthenticationConverter.BEARER + s;
                            }
                            return "";
                        })) // param with key "authorization"
                .switchIfEmpty(Mono.defer(() -> {
                    // If the access token is not found in the query parameter,
                    // check the Authorization header as a fallback
                    return Mono.justOrEmpty(serverWebExchange.getRequest()
                            .getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION));
                }));
    }

}