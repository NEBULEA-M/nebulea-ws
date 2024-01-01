package com.nebulea.ws.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * AbstractAuthenticationToken : Base class for Authentication objects.
 */
public class CurrentUserAuthenticationToken extends AbstractAuthenticationToken {

    private final CustomUserDetails userDetails;

    private final String token;

    @Override
    public Object getCredentials() {
        return this.token;
    }

    @Override
    public Object getPrincipal() {
        return this.userDetails.getUsername();
    }

    public CurrentUserAuthenticationToken(CustomUserDetails userDetails) {
        super(userDetails.getAuthorities());
        this.token = userDetails.getAccessToken();
        this.userDetails = userDetails;
        super.setAuthenticated(Boolean.TRUE);
    }

}