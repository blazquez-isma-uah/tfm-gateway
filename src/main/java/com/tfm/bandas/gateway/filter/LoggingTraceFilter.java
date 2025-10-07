package com.tfm.bandas.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
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
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            exchange.getRequest().mutate()
                    .header("X-Request-Id", traceId)
                    .build();
        }

        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String authUser = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        log.debug("[LoggingTraceFilter] Incoming request: method={} path={} traceId={} user={}",
                method, path, traceId, authUser != null ? authUser : "anonymous");

        String finalTraceId = traceId;
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - start;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("[LoggingTraceFilter] Completed: method={} path={} status={} duration={}ms traceId={}",
                            method, path, statusCode, duration, finalTraceId);
                })
        );
    }

    @Override
    public int getOrder() {
        return -1; // se ejecuta al principio
    }
}
