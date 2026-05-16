package com.s21.devops.sample.reportservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter rabbitMessagesProcessedCounter(MeterRegistry registry) {
        return Counter.builder("app_rabbitmq_messages_processed_total")
                .description("Сколько сообщений обработано из RabbitMQ")
                .register(registry);
    }
}
