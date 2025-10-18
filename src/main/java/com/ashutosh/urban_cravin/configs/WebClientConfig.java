package com.ashutosh.urban_cravin.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${phonepe.base-url}")
    private String phonePeBaseUrl;

    @Bean
    public WebClient phonePeWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(phonePeBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}