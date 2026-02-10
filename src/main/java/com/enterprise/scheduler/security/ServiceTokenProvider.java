package com.enterprise.scheduler.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

@Component
public class ServiceTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(ServiceTokenProvider.class);
    private static final String REGISTRATION_ID = "controller-app";
    private static final String PRINCIPAL_NAME = "schedulerapp-service";

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public ServiceTokenProvider(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    public String getAccessToken() {
        log.debug("Requesting access token for registration '{}'", REGISTRATION_ID);

        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId(REGISTRATION_ID)
                .principal(PRINCIPAL_NAME)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(request);

        if (authorizedClient == null) {
            log.error("Failed to obtain authorized client for registration '{}'", REGISTRATION_ID);
            throw new IllegalStateException(
                    "OAuth2 client authorization failed for registration: " + REGISTRATION_ID);
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        log.debug("Access token obtained, expires at {}", accessToken.getExpiresAt());

        return accessToken.getTokenValue();
    }
}
