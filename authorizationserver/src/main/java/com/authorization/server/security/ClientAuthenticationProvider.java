package com.authorization.server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ClientAuthenticationProvider implements AuthenticationProvider {

    private final RegisteredClientRepository registeredClientRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var clientRefreshTokenAuthentication = (ClientRefreshTokenAuthentication) authentication;
        if (!ClientAuthenticationMethod.NONE.equals(clientRefreshTokenAuthentication.getClientAuthenticationMethod())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Authentication method is not valid", null));
        }
        var clientId = clientRefreshTokenAuthentication.getPrincipal().toString();
        var registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null){
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Client is not valid", null));
        }
        if (!registeredClient.getClientAuthenticationMethods().contains(clientRefreshTokenAuthentication.getClientAuthenticationMethod())){
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT, "Authentication method is not valid", null));
        }
        return new ClientRefreshTokenAuthentication(registeredClient);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientRefreshTokenAuthentication.class.isAssignableFrom(authentication);
    }
}
