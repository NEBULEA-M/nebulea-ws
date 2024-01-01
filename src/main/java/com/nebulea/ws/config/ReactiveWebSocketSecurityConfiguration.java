package com.nebulea.ws.config;

import com.nebulea.ws.security.BearerTokenReactiveAuthenticationManager;
import com.nebulea.ws.security.ServerHttpBearerAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
public class ReactiveWebSocketSecurityConfiguration {


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityWebFilterChain authorizationServerSecurityFilterChain(ServerHttpSecurity http) {
        // @formatter:off
        http
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.anyExchange().authenticated())
                .addFilterAt(bearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
        ;
        // @formatter:on
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // @formatter:off
        http
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(authorizeExchange -> {
                    authorizeExchange.pathMatchers(HttpMethod.DELETE)
                            .denyAll();
                    authorizeExchange.anyExchange().authenticated();
                });
        // @formatter:on
        return http.build();
    }

    @Autowired
    ServerHttpBearerAuthenticationConverter serverHttpBearerAuthenticationConverter;

    @Autowired
    BearerTokenReactiveAuthenticationManager bearerTokenReactiveAuthenticationManager;

    /**
     * Spring security works by filter chaining.
     * We need to add a JWT CUSTOM FILTER to the chain.
     * <p>
     * what is AuthenticationWebFilter:
     * <p>
     * A WebFilter that performs authentication of a particular request. An outline of the logic:
     * A request comes in and if it does not match setRequiresAuthenticationMatcher(ServerWebExchangeMatcher),
     * then this filter does nothing and the WebFilterChain is continued.
     * If it does match then... An attempt to convert the ServerWebExchange into an Authentication is made.
     * If the result is empty, then the filter does nothing more and the WebFilterChain is continued.
     * If it does create an Authentication...
     * The ReactiveAuthenticationManager specified in AuthenticationWebFilter(ReactiveAuthenticationManager) is used to perform authentication.
     * If authentication is successful, ServerAuthenticationSuccessHandler is invoked and the authentication is set on ReactiveSecurityContextHolder,
     * else ServerAuthenticationFailureHandler is invoked
     */
    private AuthenticationWebFilter bearerAuthenticationFilter() {
        ReactiveAuthenticationManager authManager = bearerTokenReactiveAuthenticationManager;

        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authManager);

        ServerAuthenticationConverter bearerConverter = serverHttpBearerAuthenticationConverter;

        bearerAuthenticationFilter.setServerAuthenticationConverter(bearerConverter);
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));

        return bearerAuthenticationFilter;
    }


}
