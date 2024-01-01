package com.nebulea.ws.security;

import com.nebulea.ws.entity.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import reactor.core.publisher.Mono;

@Slf4j
public class CurrentUserAuthenticationBearer {

    public static Mono<Authentication> create(CustomUserDetails userDetails) {

        return Mono.justOrEmpty(new CurrentUserAuthenticationToken(userDetails));
    }

    public static Mono<Authentication> create(OAuth2AuthenticatedPrincipal oAuth2AuthenticatedPrincipal) {
        oAuth2AuthenticatedPrincipal.getAuthorities();
        return Mono.justOrEmpty(new CurrentUserAuthenticationToken(
                new CustomUserDetails(
                        new UserAccount()
                )
        ));
    }
}