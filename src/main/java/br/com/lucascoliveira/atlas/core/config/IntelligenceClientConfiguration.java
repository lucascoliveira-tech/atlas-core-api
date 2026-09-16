package br.com.lucascoliveira.atlas.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class IntelligenceClientConfiguration {

    @Bean
    RestClient intelligenceRestClient(
        RestClient.Builder builder,
        @Value("${atlas.intelligence.base-url}") String baseUrl,
        @Value("${atlas.intelligence.connect-timeout:1s}") Duration connectTimeout,
        @Value("${atlas.intelligence.read-timeout:10s}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return builder
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .build();
    }
}
