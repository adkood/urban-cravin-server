package com.ashutosh.urban_cravin.filters;

import com.ashutosh.urban_cravin.metrics.PerformanceMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class MetricsFilter extends HttpFilter {

    private final PerformanceMetrics metrics;

    public MetricsFilter(PerformanceMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        metrics.incrementConcurrentRequests();
        long start = System.nanoTime();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.nanoTime() - start;
            metrics.recordRequestLatency(duration);
            metrics.decrementConcurrentRequests();
        }
    }
}
