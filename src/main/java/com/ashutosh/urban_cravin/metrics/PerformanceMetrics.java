package com.ashutosh.urban_cravin.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

@Component
public class PerformanceMetrics {

    private final AtomicInteger concurrentRequests = new AtomicInteger(0);
    private final Timer requestTimer;
//    private final Timer dbQueryTimer;

    public PerformanceMetrics(MeterRegistry registry, JdbcTemplate jdbcTemplate) {
        // Request latency timer
        this.requestTimer = Timer.builder("performance.request.latency")
                .description("Latency for HTTP requests")
                .publishPercentiles(0.5, 0.9, 0.95)
                .register(registry);

        // Concurrent requests gauge
        Gauge.builder("performance.concurrent.requests", concurrentRequests, AtomicInteger::get)
                .description("Number of concurrent HTTP requests")
                .register(registry);

        // DB query timer
//        this.dbQueryTimer = Timer.builder("performance.db.query.time")
//                .description("Time taken for DB queries")
//                .publishPercentiles(0.5, 0.9, 0.95)
//                .register(registry);

    }

    // Called when request starts
    public void incrementConcurrentRequests() {
        concurrentRequests.incrementAndGet();
    }

    // Called when request ends
    public void decrementConcurrentRequests() {
        concurrentRequests.decrementAndGet();
    }

    // Record request latency
    public void recordRequestLatency(long durationNanos) {
        requestTimer.record(durationNanos, TimeUnit.NANOSECONDS);
    }

    // Record DB query time
//    public void recordDbQuery(long durationNanos) {
//        dbQueryTimer.record(durationNanos, TimeUnit.NANOSECONDS);
//    }
}
