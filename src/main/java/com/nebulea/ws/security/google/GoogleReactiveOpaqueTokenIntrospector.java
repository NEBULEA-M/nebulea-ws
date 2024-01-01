package com.nebulea.ws.security.google;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Audience;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GoogleReactiveOpaqueTokenIntrospector implements ReactiveOpaqueTokenIntrospector {

    private final Log logger = LogFactory.getLog(this.getClass());
    private final URI introspectionUri;
    private final String clientId;
    private final WebClient webClient;

    private final String TOKEN_TYPE = "id_token";

    public GoogleReactiveOpaqueTokenIntrospector(String introspectionUri, String clientId, String clientSecret) {
        Assert.hasText(introspectionUri, "introspectionUri cannot be empty");
        Assert.hasText(clientId, "clientId cannot be empty");
        Assert.notNull(clientSecret, "clientSecret cannot be null");
        this.clientId = clientId;
        this.introspectionUri = URI.create(introspectionUri);
        this.webClient = WebClient.builder().defaultHeaders((h) -> {
            h.setBasicAuth(clientId, clientSecret);
        }).build();
    }

    @Override
    public Mono<OAuth2AuthenticatedPrincipal> introspect(String token) {
        return Mono.just(token)
                .flatMap(this::makeRequest)
                .flatMap(this::adaptToNimbusResponse)
                .map(this::parseNimbusResponse)
                .map(this::castToNimbusSuccess)
                .doOnNext((response) ->
                        this.validate(response, this.clientId))
                .map(this::convertClaimsSet)
                .onErrorMap((e) -> !(e instanceof OAuth2IntrospectionException), this::onError);
    }

    private Mono<ClientResponse> makeRequest(String token) {
        return this.webClient.post()
                .uri(this.introspectionUri)
                .header("Accept", new String[]{"application/json"})
                .body(BodyInserters.fromFormData(TOKEN_TYPE, token))
                .exchange();
    }

    private Mono<HTTPResponse> adaptToNimbusResponse(ClientResponse responseEntity) {
        MediaType contentType = responseEntity.headers().contentType().orElseThrow(() -> {
            this.logger.trace("Did not receive Content-Type from introspection endpoint in response");
            return new OAuth2IntrospectionException("Introspection endpoint response was invalid, as no Content-Type header was provided");
        });
        if (!contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            this.logger.trace("Did not receive JSON-compatible Content-Type from introspection endpoint in response");
            throw new OAuth2IntrospectionException("Introspection endpoint response was invalid, as content type '" + contentType + "' is not compatible with JSON");
        } else {
            HTTPResponse response = new HTTPResponse(responseEntity.statusCode().value());
            response.setHeader("Content-Type", contentType.toString());
            if (response.getStatusCode() != 200) {
                this.logger.trace("Introspection endpoint returned non-OK status code");
                return responseEntity.bodyToFlux(DataBuffer.class).map(DataBufferUtils::release).then(Mono.error(new OAuth2IntrospectionException("Introspection endpoint responded with HTTP status code " + response.getStatusCode())));
            } else {
                Mono<String> var10000 = responseEntity.bodyToMono(String.class);
                Objects.requireNonNull(response);
                return var10000.doOnNext(response::setContent)
                        .map((body) -> response);
            }
        }
    }

    private GoogleTokenInfoResponse parseNimbusResponse(HTTPResponse response) {
        try {
            return GoogleTokenInfoResponse.parse(response);
        } catch (Exception var3) {
            throw new OAuth2IntrospectionException(var3.getMessage(), var3);
        }
    }

    private GoogleTokenInfoSuccessResponse castToNimbusSuccess(GoogleTokenInfoResponse introspectionResponse) {
        if (!introspectionResponse.indicatesSuccess()) {
            ErrorObject errorObject = introspectionResponse.toErrorResponse().getErrorObject();
            String message = "Token introspection failed with response " + errorObject.toJSONObject().toJSONString();
            this.logger.trace(message);
            throw new OAuth2IntrospectionException(message);
        } else {
            return (GoogleTokenInfoSuccessResponse) introspectionResponse;
        }
    }

    private void validate(GoogleTokenInfoSuccessResponse response, String clientId) {
        if (!response.isActive(clientId)) {
            this.logger.trace("Did not validate token since it is inactive");
            throw new BadOpaqueTokenException("Provided token isn't active");
        }
    }

    private OAuth2AuthenticatedPrincipal convertClaimsSet(GoogleTokenInfoSuccessResponse response) {
        Map<String, Object> claims = response.toJSONObject();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Iterator var5;
        if (response.getAudience() != null) {
            List<String> audiences = new ArrayList<>();
            var5 = response.getAudience().iterator();

            while (var5.hasNext()) {
                Audience audience = (Audience) var5.next();
                audiences.add(audience.getValue());
            }

            claims.put("aud", Collections.unmodifiableList(audiences));
        }

        if (response.getClientID() != null) {
            claims.put("client_id", response.getClientID().getValue());
        }

        Instant iat;
        if (response.getExpirationTime() != null) {
            iat = response.getExpirationTime().toInstant();
            claims.put("exp", iat);
        }

        if (response.getIssueTime() != null) {
            iat = response.getIssueTime().toInstant();
            claims.put("iat", iat);
        }

        if (response.getIssuer() != null) {
            claims.put("iss", response.getIssuer().getValue());
        }

        return new OAuth2IntrospectionAuthenticatedPrincipal(claims, authorities);
    }

    private OAuth2IntrospectionException onError(Throwable ex) {
        return new OAuth2IntrospectionException(ex.getMessage(), ex);
    }
}
