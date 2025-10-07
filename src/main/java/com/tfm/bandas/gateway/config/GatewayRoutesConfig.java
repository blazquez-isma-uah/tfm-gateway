package com.tfm.bandas.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${gateway.users.path}")
    private String usersPath;
    @Value("${gateway.instruments.path}")
    private String instrumentsPath;
    @Value("${gateway.events.path}")
    private String eventsPath;
    @Value("${gateway.scores.path}")
    private String scoresPath;
    @Value("${gateway.surveys.path}")
    private String surveysPath;

    @Value("${gateway.users.uri}")
    private String usersUri;
    @Value("${gateway.events.uri}")
    private String eventsUri;
    @Value("${gateway.scores.uri}")
    private String scoresUri;
    @Value("${gateway.surveys.uri}")
    private String surveysUri;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // USERS
                .route("users", r -> r.path(usersPath, instrumentsPath)
                        .filters(f -> f.removeRequestHeader("Cookie"))
                        .uri(usersUri))
                // EVENTS
                .route("events", r -> r.path(eventsPath)
                        .filters(f -> f.removeRequestHeader("Cookie"))
                        .uri(eventsUri))
                // SCORES
                .route("scores", r -> r.path(scoresPath)
                        .filters(f -> f.removeRequestHeader("Cookie"))
                        .uri(scoresUri))
                // SURVEYS
                .route("surveys", r -> r.path(surveysPath)
                        .filters(f -> f.removeRequestHeader("Cookie"))
                        .uri(surveysUri))
                .build();
    }
}
