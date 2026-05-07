package com.coupleapp.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// Custom metrics service for tracking business-critical events.
// Metrics are exposed at /actuator/prometheus for Prometheus scraping.
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // --- Cache metrics ---

    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hit")
                .tag("cache", cacheName)
                .description("Number of cache hits")
                .register(meterRegistry)
                .increment();
    }

    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.miss")
                .tag("cache", cacheName)
                .description("Number of cache misses")
                .register(meterRegistry)
                .increment();
    }

    // --- Rate limiting metrics ---

    public void recordRateLimitExceeded(String endpoint) {
        Counter.builder("rate_limit.exceeded")
                .tag("endpoint", endpoint)
                .description("Number of requests blocked by rate limiter")
                .register(meterRegistry)
                .increment();
    }

    public void recordRateLimitAllowed(String endpoint) {
        Counter.builder("rate_limit.allowed")
                .tag("endpoint", endpoint)
                .description("Number of requests allowed by rate limiter")
                .register(meterRegistry)
                .increment();
    }

    // --- External API metrics ---

    public void recordExternalApiCall(String apiName, long durationMs, boolean success) {
        // Record call count
        Counter.builder("external_api.calls")
                .tag("api", apiName)
                .tag("status", success ? "success" : "failure")
                .description("Number of external API calls")
                .register(meterRegistry)
                .increment();

        // Record latency
        Timer.builder("external_api.duration")
                .tag("api", apiName)
                .description("External API call duration")
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    // --- Business metrics ---

    public void recordSuggestionSearch(String businessType, int resultCount) {
        Counter.builder("suggestions.search")
                .tag("type", businessType)
                .description("Number of suggestion searches performed")
                .register(meterRegistry)
                .increment();

        // Record result count distribution
        meterRegistry.summary("suggestions.results")
                .record(resultCount);
    }
}
