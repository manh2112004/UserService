package org.User.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Value("${keycloak.admin.username}")
    private String adminUser;
    @Value("${keycloak.admin.password}")
    private String adminPass;

    @Value("${keycloak.server.url}")
    private String serverUrl;
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUser)
                .password(adminPass)
                .build();
    }
}
