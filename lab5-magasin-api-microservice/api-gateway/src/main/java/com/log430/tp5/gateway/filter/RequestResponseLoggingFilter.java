package com.log430.tp5.gateway.filter;

import java.net.URI;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Global filter to log all incoming requests and outgoing responses
 * through the API Gateway.
 */
@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
        "authorization", "cookie", "x-api-key", "x-auth-token"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Log incoming request
        logIncomingRequest(request);
        
        // Start timing the request
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
            .doFinally(signalType -> {
                // Log outgoing response
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;
                logOutgoingResponse(request, response, duration);
            });
    }

    private void logIncomingRequest(ServerHttpRequest request) {
        HttpMethod method = request.getMethod();
        URI uri = request.getURI();
        String path = uri.getPath();
        String query = uri.getQuery();
        String remoteAddress = getClientIpAddress(request);
        
        logger.info("==> INCOMING REQUEST: {} {} from {} {}",
            method, 
            path,
            remoteAddress,
            query != null ? "?" + query : ""
        );
        
        // Log headers (excluding sensitive ones)
        request.getHeaders().forEach((name, values) -> {
            if (!SENSITIVE_HEADERS.contains(name.toLowerCase())) {
                logger.info("    Header: {} = {}", name, String.join(", ", values));
            } else {
                logger.info("    Header: {} = [HIDDEN]", name);
            }
        });
    }

    private void logOutgoingResponse(ServerHttpRequest request, ServerHttpResponse response, long duration) {
        HttpMethod method = request.getMethod();
        String path = request.getURI().getPath();
        HttpStatusCode status = response.getStatusCode();
        String remoteAddress = getClientIpAddress(request);
        
        if (status != null) {
            int statusValue = status.value();
            if (statusValue >= 200 && statusValue < 300) {
                logger.info("<== RESPONSE: {} {} from {} - {} - {}ms",
                    method, path, remoteAddress, statusValue, duration);
            } else if (statusValue >= 400 && statusValue < 500) {
                logger.warn("<== ERROR RESPONSE: {} {} from {} - {} - {}ms",
                    method, path, remoteAddress, statusValue, duration);
            } else if (statusValue >= 500) {
                logger.error("<== SERVER ERROR: {} {} from {} - {} - {}ms",
                    method, path, remoteAddress, statusValue, duration);
            } else {
                logger.info("<== RESPONSE: {} {} from {} - {} - {}ms",
                    method, path, remoteAddress, statusValue, duration);
            }
        } else {
            logger.warn("<== RESPONSE: {} {} from {} - NO STATUS - {}ms",
                method, path, remoteAddress, duration);
        }
        
        // Log response headers (excluding sensitive ones)
        response.getHeaders().forEach((name, values) -> {
            if (!SENSITIVE_HEADERS.contains(name.toLowerCase())) {
                logger.debug("    Response Header: {} = {}", name, String.join(", ", values));
            }
        });
    }

    @SuppressWarnings("null")
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        var remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            var address = remoteAddress.getAddress();
            if (address != null) {
                return address.getHostAddress();
            }
        }
        
        return "unknown";
    }

    @Override
    public int getOrder() {
        // Execute this filter early in the chain to log all requests
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
