package com.ashutosh.urban_cravin.configs;

import com.ashutosh.urban_cravin.filters.MetricsFilter;
import com.ashutosh.urban_cravin.metrics.PerformanceMetrics;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<MetricsFilter> metricsFilter(PerformanceMetrics metrics) {
        FilterRegistrationBean<MetricsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MetricsFilter(metrics));
        registration.addUrlPatterns("/*"); // apply to all requests
        registration.setOrder(1); // ensure it runs early
        return registration;
    }
}
