package com.okta.developer.cli;

import com.okta.sdk.client.Client;
import com.okta.sdk.lang.Collections;
import com.okta.sdk.resource.application.OpenIdConnectApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class AppRedirectUriManager implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AppRedirectUriManager.class);

    private final Client client;

    @Value("${appId}")
    private String appId;

    @Value("${redirectUri}")
    private String redirectUri;

    @Value("${operation:add}")
    private String operation;

    public AppRedirectUriManager(Client client) {
        this.client = client;
    }

    public static void main(String[] args) {
        SpringApplication.run(AppRedirectUriManager.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Adjusting Okta settings: {appId: {}, redirectUri: {}, operation: {}}", appId, redirectUri, operation);
        OpenIdConnectApplication app = (OpenIdConnectApplication) client.getApplication(appId);

        String loginRedirectUri = redirectUri + "/login";

        // update redirect URIs
        List<String> redirectUris = app.getSettings().getOAuthClient().getRedirectUris();
        // use a set so values are unique
        Set<String> updatedRedirectUris = new LinkedHashSet<>(redirectUris);
        if (operation.equalsIgnoreCase("add")) {
            updatedRedirectUris.add(loginRedirectUri);
        } else if (operation.equalsIgnoreCase("remove")) {
            updatedRedirectUris.remove(loginRedirectUri);
        }

        // todo: update logout redirect URIs with redirectUri (not currently available in Java SDK)
        // https://github.com/okta/openapi/issues/132
        app.getSettings().getOAuthClient().setRedirectUris(Collections.toList(updatedRedirectUris));
        app.update();
        System.exit(0);
    }
}
