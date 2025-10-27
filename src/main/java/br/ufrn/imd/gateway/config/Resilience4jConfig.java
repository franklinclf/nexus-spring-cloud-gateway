package br.ufrn.imd.gateway.config;

import br.ufrn.imd.gateway.exception.ServiceCallException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(MeterRegistry meterRegistry) {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

        // Registrar métricas para Prometheus
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry)
                .bindTo(meterRegistry);

        // Circuit Breaker para Nexus (timeout: 500ms)
        registry.circuitBreaker("nexusCircuitBreaker", CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slowCallDurationThreshold(Duration.ofMillis(500))
                .slowCallRateThreshold(50.0f)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                        IOException.class,
                        TimeoutException.class,
                        ConnectException.class,
                        SocketTimeoutException.class
                )
                .recordException(e -> {
                    if (e instanceof HttpServerErrorException serverError) {
                        int statusCode = serverError.getStatusCode().value();
                        return statusCode == 502 || // BAD_GATEWAY
                                statusCode == 504 || // GATEWAY_TIMEOUT
                                statusCode == 500;   // INTERNAL_SERVER_ERROR
                    }
                    if (e instanceof ServiceCallException && e.getCause() != null) {
                        return recordByRootCause(e.getCause());
                    }
                    return false;
                })
                .ignoreExceptions(HttpClientErrorException.class)
                .build());

        // Circuit Breaker para AI (timeout: 30s)
        registry.circuitBreaker("aiCircuitBreaker", CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .slowCallDurationThreshold(Duration.ofSeconds(30))
                .slowCallRateThreshold(50.0f)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                        IOException.class,
                        TimeoutException.class
                )
                .recordException(e -> {
                    if (e instanceof HttpServerErrorException serverError) {
                        int statusCode = serverError.getStatusCode().value();
                        return statusCode == 502 || // BAD_GATEWAY
                                statusCode == 504 || // GATEWAY_TIMEOUT
                                statusCode == 500;   // INTERNAL_SERVER_ERROR
                    }
                    if (e instanceof ServiceCallException && e.getCause() != null) {
                        return recordByRootCause(e.getCause());
                    }
                    return false;
                })
                .ignoreExceptions(HttpClientErrorException.class)
                .build());

        // Circuit Breaker para Tools (timeout: 30s)
        registry.circuitBreaker("toolsCircuitBreaker", CircuitBreakerConfig.custom()
                .slidingWindowSize(15)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .minimumNumberOfCalls(8)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(20))
                .permittedNumberOfCallsInHalfOpenState(4)
                .slowCallDurationThreshold(Duration.ofSeconds(30))
                .slowCallRateThreshold(50.0f)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                        IOException.class,
                        TimeoutException.class
                )
                .recordException(e -> {
                    if (e instanceof HttpServerErrorException serverError) {
                        int statusCode = serverError.getStatusCode().value();
                        return statusCode == 502 || // BAD_GATEWAY
                                statusCode == 504 || // GATEWAY_TIMEOUT
                                statusCode == 500;   // INTERNAL_SERVER_ERROR
                    }
                    if (e instanceof ServiceCallException && e.getCause() != null) {
                        return recordByRootCause(e.getCause());
                    }
                    return false;
                })
                .ignoreExceptions(HttpClientErrorException.class)
                .build());

        return registry;
    }

    private static boolean recordByRootCause(Throwable cause) {
        return cause instanceof IOException || cause instanceof TimeoutException;
    }

    @Bean
    public RetryRegistry retryRegistry(MeterRegistry meterRegistry) {
        RetryRegistry registry = RetryRegistry.ofDefaults();

        // Registrar métricas para Prometheus
        TaggedRetryMetrics.ofRetryRegistry(registry)
                .bindTo(meterRegistry);

        // Retry para Nexus (5 tentativas, backoff exponencial: 100ms -> 800ms)
        registry.retry("nexusRetry", RetryConfig.custom()
                .maxAttempts(5)
                .waitDuration(Duration.ofMillis(100))
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(100), // initialInterval
                        2.0,                     // multiplier (factor)
                        Duration.ofMillis(800)   // maxInterval
                ))
                .retryOnException(e -> {
                    if (e instanceof HttpServerErrorException serverError) {
                        int statusCode = serverError.getStatusCode().value();
                        return statusCode == 502 || // BAD_GATEWAY
                                statusCode == 504 || // GATEWAY_TIMEOUT
                                statusCode == 500;   // INTERNAL_SERVER_ERROR
                    }
                    if (e instanceof ServiceCallException && e.getCause() != null) {
                        return retryOnRootCause(e.getCause());
                    }
                    return e instanceof IOException ||
                            e instanceof TimeoutException;
                })
                .ignoreExceptions(HttpClientErrorException.class)
                .failAfterMaxAttempts(true)
                .build());

        // Retry para AI (20 tentativas, backoff exponencial: 100ms -> 800ms)
        registry.retry("aiRetry", RetryConfig.custom()
                .maxAttempts(20)
                .waitDuration(Duration.ofMillis(100))
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(100),
                        2.0,
                        Duration.ofMillis(800)
                ))
                .retryOnException(e -> {
                    if (e instanceof HttpServerErrorException serverError) {
                        int statusCode = serverError.getStatusCode().value();
                        return statusCode == 502 || // BAD_GATEWAY
                                statusCode == 504 || // GATEWAY_TIMEOUT
                                statusCode == 500;   // INTERNAL_SERVER_ERROR
                    }
                    if (e instanceof ServiceCallException && e.getCause() != null) {
                        return retryOnRootCause(e.getCause());
                    }
                    return e instanceof IOException ||
                            e instanceof TimeoutException;
                })
                .ignoreExceptions(HttpClientErrorException.class)
                .failAfterMaxAttempts(true)
                .build());

        // Retry para Tools (15 tentativas, backoff exponencial: 100ms -> 800ms)
        registry.retry("toolsRetry", RetryConfig.custom()
                .maxAttempts(15)
                .waitDuration(Duration.ofMillis(100))
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(100),
                        2.0,
                        Duration.ofMillis(800)
                ))
                .retryOnException(e -> {
                    if (e instanceof HttpServerErrorException serverError) {
                        int statusCode = serverError.getStatusCode().value();
                        return statusCode == 502 || // BAD_GATEWAY
                                statusCode == 504 || // GATEWAY_TIMEOUT
                                statusCode == 500;   // INTERNAL_SERVER_ERROR
                    }
                    if (e instanceof ServiceCallException && e.getCause() != null) {
                        return retryOnRootCause(e.getCause());
                    }
                    return e instanceof IOException ||
                            e instanceof TimeoutException;
                })
                .ignoreExceptions(HttpClientErrorException.class)
                .failAfterMaxAttempts(true)
                .build());

        return registry;
    }

    private static boolean retryOnRootCause(Throwable cause) {
        return cause instanceof IOException || cause instanceof TimeoutException;
    }
}