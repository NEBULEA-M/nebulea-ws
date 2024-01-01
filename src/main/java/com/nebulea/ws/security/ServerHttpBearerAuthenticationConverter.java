package com.nebulea.ws.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This is a Converter that validates TOKEN against requests coming from AuthenticationFilter ServerWebExchange.
 */
@Component
public class ServerHttpBearerAuthenticationConverter implements ServerAuthenticationConverter {
    @Value("${security.oauth2.provider.google.token-info}")
    private String introspectionUri;

    @Value("${security.oauth2.provider.google.client-id}")
    private String clientId;

    @Value("${security.oauth2.provider.google.client-secret}")
    private String clientSecret;

    public static final String AUTHORIZATION_PARAM = "authorization";
    public static final String BEARER = "Bearer ";
    private static final Predicate<String> matchBearerLength = authValue -> authValue.length() > BEARER.length();
    private static final Function<String, Mono<String>> isolateBearerValue = authValue -> Mono.justOrEmpty(authValue.substring(BEARER.length()));

    @Autowired
    private CustomRemoteTokenHandler jwtVerifier;

    @Override
    public Mono<Authentication> convert(ServerWebExchange serverWebExchange) {
        System.out.println(serverWebExchange);;
        jwtVerifier.setDelegate(introspectionUri, clientId, clientSecret);

        return Mono.justOrEmpty(serverWebExchange)
                .flatMap(AuthorizationHeaderPayload::extract)
                .filter(matchBearerLength)
                .flatMap(isolateBearerValue)
                .flatMap(jwtVerifier::introspectToken)
                .flatMap(CurrentUserAuthenticationBearer::create).log();


    }

}