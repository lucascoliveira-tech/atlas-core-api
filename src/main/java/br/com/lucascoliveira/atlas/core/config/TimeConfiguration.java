package br.com.lucascoliveira.atlas.core.config;

import java.time.Clock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TimeConfiguration {

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
