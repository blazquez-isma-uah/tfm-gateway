package com.tfm.bandas.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Filtro global que añade un X-Request-Id si no existe y loguea cada petición entrante y su respuesta.
 */
@Component
public class LoggingTraceFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingTraceFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long start = System.currentTimeMillis();

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String traceId = headers.getFirst("X-Request-Id");
        ServerWebExchange finalExchange;

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            final String finalTraceId = traceId;
            finalExchange = exchange.mutate()
                    .request(r -> r.header("X-Request-Id", finalTraceId))
                    .build();
        } else {
            finalExchange = exchange;
        }

        String method = finalExchange.getRequest().getMethod().name();
        String path = finalExchange.getRequest().getURI().getPath();
        String authUser = finalExchange.getRequest().getHeaders().getFirst("X-User-Id");

        log.debug("[LoggingTraceFilter] Incoming request: method={} path={} traceId={} user={}",
                method, path, traceId, authUser != null ? authUser : "anonymous");

        final String finalTraceIdForLogging = traceId;
        return chain.filter(finalExchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - start;
                    int statusCode = finalExchange.getResponse().getStatusCode() != null
                            ? finalExchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("[LoggingTraceFilter] Completed: method={} path={} status={} duration={}ms traceId={}",
                            method, path, statusCode, duration, finalTraceIdForLogging);
                })
        );
    }

    @Override
    public int getOrder() {
        return -1; // se ejecuta al principio
    }
}
