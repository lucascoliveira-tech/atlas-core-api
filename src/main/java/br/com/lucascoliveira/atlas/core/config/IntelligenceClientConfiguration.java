package br.com.lucascoliveira.atlas.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class IntelligenceClientConfiguration {

    @Bean
    RestClient intelligenceRestClient(
        RestClient.Builder builder,
        @Value("${atlas.intelligence.base-url}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}

