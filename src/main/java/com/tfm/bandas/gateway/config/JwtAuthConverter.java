package com.tfm.bandas.gateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Convierte los claims de roles/grupos del JWT en GrantedAuthority de Spring Security.
 * Implementación reactiva (WebFlux) para Spring Cloud Gateway.
 * <p>
 * Soporta dos proveedores:
 *   - Amazon Cognito (perfil aws):   claim "cognito:groups"
 *   - Keycloak       (perfil local): claim "realm_access.roles"
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        // ── Cognito (AWS) ─────────────────────────────────────────────────
        List<String> cognitoGroups = jwt.getClaimAsStringList("cognito:groups");
        if (cognitoGroups != null && !cognitoGroups.isEmpty()) {
            return Flux.fromIterable(cognitoGroups)
                    .filter(Objects::nonNull)
                    .map(g -> new SimpleGrantedAuthority("ROLE_" + g.toUpperCase()));
        }

        // ── Keycloak (local) ──────────────────────────────────────────────
        var realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
            return Flux.fromIterable(roles)
                    .filter(Objects::nonNull)
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toString().toUpperCase()));
        }

        return Flux.empty();
    }
}