package br.ufrn.imd.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import br.ufrn.imd.gateway.exception.ServiceCallException;
import br.ufrn.imd.gateway.exception.ErrorResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
import java.util.function.Function;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class GatewayMvcConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayMvcConfig.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public GatewayMvcConfig(CircuitBreakerRegistry circuitBreakerRegistry,
                            RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {
        return route("nexus")
                .route(RequestPredicates.path("/nexus/**"), request ->
                        executeWithResilience(
                                request,
                                "lb://Nexus",
                                "nexusCircuitBreaker",
                                "nexusRetry",
                                "Nexus"
                        )
                )
                .filter(stripPrefix(1))
                .build()

                .and(route("ai")
                        .route(RequestPredicates.path("/ai/**"), request ->
                                executeWithResilience(
                                        request,
                                        "lb://AI",
                                        "aiCircuitBreaker",
                                        "aiRetry",
                                        "AI"
                                )
                        )
                        .filter(stripPrefix(1))
                        .build())

                .and(route("tools")
                        .route(RequestPredicates.path("/tools/**"), request ->
                                executeWithResilience(
                                        request,
                                        "lb://MCP",
                                        "toolsCircuitBreaker",
                                        "toolsRetry",
                                        "Tools"
                                )
                        )
                        .filter(stripPrefix(1))
                        .build());
    }

    private ServerResponse executeWithResilience(
            ServerRequest request,
            String targetUri,
            String circuitBreakerName,
            String retryName,
            String serviceName) {

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
        Retry retry = retryRegistry.retry(retryName);

        // Cria função que pode lançar exceção
        Function<ServerRequest, ServerResponse> proxyFunction = req -> {
            try {
                return HandlerFunctions.http(new URI(targetUri)).handle(req);
            } catch (Exception e) {
                log.error("Error calling service {} for request {}: {}",
                        serviceName, req.path(), e.getMessage());
                throw new ServiceCallException("Failed to call " + serviceName, e);
            }
        };

        // Decora com Retry e Circuit Breaker
        Function<ServerRequest, ServerResponse> decoratedFunction =
                CircuitBreaker.decorateFunction(circuitBreaker,
                        Retry.decorateFunction(retry, proxyFunction));

        try {
            return decoratedFunction.apply(request);
        } catch (Exception e) {
            log.error("Service {} is unavailable after retries and circuit breaker: {}",
                    serviceName, e.getMessage(), e);

            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorResponse(
                            serviceName + " service is temporarily unavailable",
                            "Please try again later",
                            e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
        }
    }
}