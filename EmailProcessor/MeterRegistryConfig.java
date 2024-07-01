package org.example.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class MeterRegistryConfig {

    @Bean
    @Lazy
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}