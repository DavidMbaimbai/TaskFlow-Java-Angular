package com.authorization.server.security;

import com.authorization.server.utils.UserUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

public class UserJwtGenerator implements OAuth2TokenGenerator<Jwt> {
    private final JwtEncoder jwtEncoder;
    private OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer;

    public UserJwtGenerator(JwtEncoder jwtEncoder) {
        Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
        this.jwtEncoder = jwtEncoder;
    }


    @Nullable
    @Override
    public Jwt generate(OAuth2TokenContext context) {
        if (context.getTokenType() != null && (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) || OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue()))) {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) && !OAuth2TokenFormat.SELF_CONTAINED.equals(context.getRegisteredClient().getTokenSettings().getAccessTokenFormat())) {
                return null;
            } else {
                String issuer = null;
                if (context.getAuthorizationServerContext() != null) {
                    issuer = context.getAuthorizationServerContext().getIssuer();
                }
                RegisteredClient registeredClient = context.getRegisteredClient();
                Instant issuedAt = Instant.now();
                JwsAlgorithm jwsAlgorithm = SignatureAlgorithm.RS256;
                Instant expiresAt;
                if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                    expiresAt = issuedAt.plus(30L, ChronoUnit.MINUTES);
                    if (registeredClient.getTokenSettings().getIdTokenSignatureAlgorithm() != null) {
                        jwsAlgorithm = registeredClient.getTokenSettings().getIdTokenSignatureAlgorithm();
                    }
                } else {
                    expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getAccessTokenTimeToLive());
                }
                JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
                if (StringUtils.hasText(issuer)) {
                    claimsBuilder.issuer(issuer);
                }
                claimsBuilder.subject(UserUtils.getUser(context.getPrincipal()).getUserUuid()).audience(Collections.singletonList(registeredClient.getClientId())).issuedAt(issuedAt).expiresAt(expiresAt).id(UUID.randomUUID().toString());
                SessionInformation sessionInformation;
                if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                    claimsBuilder.notBefore(issuedAt);
                    if (!CollectionUtils.isEmpty(context.getAuthorizedScopes())) {
                        claimsBuilder.claim("scope", context.getAuthorizedScopes());
                    }
                } else if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                    claimsBuilder.claim(IdTokenClaimNames.AZP, registeredClient.getClientId());
                    if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(context.getAuthorizationGrantType())) {
                        OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest) context.getAuthorization().getAttribute(OAuth2AuthorizationRequest.class.getName());
                        String nonce = (String) authorizationRequest.getAdditionalParameters().get("nonce");
                        if (StringUtils.hasText(nonce)) {
                            claimsBuilder.claim(IdTokenClaimNames.NONCE, nonce);
                        }
                        sessionInformation = (SessionInformation) context.get(SessionInformation.class);
                        if (sessionInformation != null) {
                            claimsBuilder.claim("sid", sessionInformation.getSessionId());
                            claimsBuilder.claim(IdTokenClaimNames.AUTH_TIME, sessionInformation.getLastRequest());
                        }
                    } else if (AuthorizationGrantType.REFRESH_TOKEN.equals(context.getAuthorizationGrantType())) {
                        OidcIdToken currentIdToken = (OidcIdToken) context.getAuthorization().getToken(OidcIdToken.class).getToken();
                        if (currentIdToken.hasClaim("sid")) {
                            claimsBuilder.claim("sid", currentIdToken.getClaim("sid"));
                        }

                        if (currentIdToken.hasClaim(IdTokenClaimNames.AUTH_TIME)) {
                            claimsBuilder.claim(IdTokenClaimNames.AUTH_TIME, currentIdToken.getClaim(IdTokenClaimNames.AUTH_TIME));
                        }
                    }
                }

                JwsHeader.Builder jwsHeaderBuilder = JwsHeader.with(jwsAlgorithm);
                if (this.jwtCustomizer != null) {
                    JwtEncodingContext.Builder jwtContextBuilder = JwtEncodingContext.with(jwsHeaderBuilder, claimsBuilder).registeredClient(context
                                    .getRegisteredClient()).principal(context.getPrincipal())
                            .authorizationServerContext(context.getAuthorizationServerContext())
                            .authorizedScopes(context.getAuthorizedScopes())
                            .tokenType(context.getTokenType())
                            .authorizationGrantType(context.getAuthorizationGrantType());
                    if (context.getAuthorization() != null) {
                        jwtContextBuilder.authorization(context.getAuthorization());
                    }

                    if (context.getAuthorizationGrant() != null) {
                        jwtContextBuilder.authorizationGrant(context.getAuthorizationGrant());
                    }

                    if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                        sessionInformation = (SessionInformation) context.get(SessionInformation.class);
                        if (sessionInformation != null) {
                            jwtContextBuilder.put(SessionInformation.class, sessionInformation);
                        }
                    }

                    JwtEncodingContext jwtContext = jwtContextBuilder.build();
                    this.jwtCustomizer.customize(jwtContext);
                }

                JwsHeader jwsHeader = jwsHeaderBuilder.build();
                JwtClaimsSet claims = claimsBuilder.build();
                Jwt jwt = this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
                return jwt;
            }
        } else {
            return null;
        }
    }

    public void setJwtCustomizer(OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
        Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
        this.jwtCustomizer = jwtCustomizer;
    }

    public static UserJwtGenerator init(JwtEncoder jwtEncoder) {
        return new UserJwtGenerator(jwtEncoder);
    }
}
