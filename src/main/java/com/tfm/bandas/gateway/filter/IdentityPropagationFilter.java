package com.tfm.bandas.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Propaga información básica de identidad en cabeceras.
 * Solo para trazabilidad y logging en servicios, no para confiar en ellos como fuente de permisos.
 */
@Component
public class IdentityPropagationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingTraceFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .flatMap(auth -> {
                    Jwt jwt = (Jwt) auth.getPrincipal();
                    log.debug("[IdentityPropagationFilter] JWT Claims: {}", jwt.getClaims());
                    String userId = jwt.getClaimAsString("sub");
                    log.debug("[IdentityPropagationFilter] userId: {}", userId);
                    String roles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));
                    log.debug("[IdentityPropagationFilter] userRoles: {}", roles);
                    exchange.getRequest().mutate()
                            .header("X-User-Id", userId != null ? userId : "")
                            .header("X-Roles", roles)
                            .build();

                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return 0; // después del LoggingTraceFilter
    }
}
