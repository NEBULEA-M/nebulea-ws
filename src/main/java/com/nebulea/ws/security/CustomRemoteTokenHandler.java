package com.nebulea.ws.security;


import com.nebulea.ws.security.google.GoogleReactiveOpaqueTokenIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Slf4j
@Component
public class CustomRemoteTokenHandler {
    private GoogleReactiveOpaqueTokenIntrospector delegate;

//    private final String tokenName = "token";


    //    @Autowired
//    private UserAccountService userAccountService;

    private String accessToken;

    public void setDelegate(String introspectionUri, String clientId, String clientSecret) {
        this.delegate = new GoogleReactiveOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    }

    public Mono<OAuth2AuthenticatedPrincipal> introspectToken(String token) {
        Mono<OAuth2AuthenticatedPrincipal> principalMono = this.delegate.introspect(token);



        return principalMono
                .flatMap(principal -> {
                    String username;
                    if (principal.getAttributes().containsKey("username")) {
                        username = principal.getAttributes().get("username").toString();
                    } else {
                        username = principal.getName();
                    }
                    return Mono.just(new DefaultOAuth2AuthenticatedPrincipal(
                            username, principal.getAttributes(), extractAuthorities(principal)));
                });
    }

    private Collection<GrantedAuthority> extractAuthorities(OAuth2AuthenticatedPrincipal principal) {

        Collection<? extends GrantedAuthority> grantedAuthorities = null;
        if (principal.getAttributes().containsKey("sub")) {
            String userSso = principal.getAttributes().get("sub").toString();

            // TODO Get priority authorities
        }
        return (Collection<GrantedAuthority>) grantedAuthorities;
    }

//    public Mono<CustomUserDetails> instrospectToken(String accessToken) {
//
//        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
//        formData.add(tokenName, accessToken);
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret));
//        Map<String, Object> map = postForMap(introspectionUri,
//                formData, headers);
//
//        if (map.containsKey("username")) {
//
//            Mono<CustomUserDetails> user = userAccountService.loadUserByUsername((String) map.get("username"));
//
//            this.accessToken = accessToken;
//            return user.flatMap(this::setAccessToken);
//        }
//        return null;
//    }

    private Mono<CustomUserDetails> setAccessToken(CustomUserDetails customUserDetails) {
        customUserDetails.setAccessToken(accessToken);
        return Mono.just(customUserDetails);
    }

//    private Map<String, Object> postForMap(String path, MultiValueMap<String, String> formData,
//                                           HttpHeaders headers) {
//        if (headers.getContentType() == null) {
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        }
//        @SuppressWarnings("rawtypes")
//        Map map = restTemplate.exchange(path, HttpMethod.POST,
//                new HttpEntity<>(formData, headers), Map.class).getBody();
//        @SuppressWarnings("unchecked")
//        Map<String, Object> result = map;
//        return result;
//    }

//    private String getAuthorizationHeader(String claeientId, String clientSecret) {
//
//        if (clientId == null || clientSecret == null) {
//            log.warn(
//                    "Null Client ID or Client Secret detected. Endpoint that requires authentication will reject request with 401 error.");
//        }
//
//        String creds = String.format("%s:%s", clientId, clientSecret);
//        try {
//            System.out.println("--------------------------------------------------------------");
//            System.out.println("getAuthorizationHeader");
//            System.out.println("--------------------------------------------------------------");
//            return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
//        } catch (UnsupportedEncodingException e) {
//            throw new IllegalStateException("Could not convert String");
//        }
//    }
}