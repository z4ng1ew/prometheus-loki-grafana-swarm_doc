package com.s21.devops.sample.bookingservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Счётчики для booking-service.
 *
 * @Configuration  — Spring при старте обработает класс и создаст бины (объекты).
 * @Bean           — каждый метод с этой аннотацией возвращает объект, который
 *                   Spring будет инжектить туда, где запросят его (через @Autowired).
 *
 * MeterRegistry  — реестр метрик Micrometer. Сам Spring Boot создаёт его
 *                  автоматически, если в зависимостях есть micrometer-registry-prometheus.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Counter bookingsCreatedCounter(MeterRegistry registry) {
        return Counter.builder("app_bookings_created_total")
                .description("Сколько бронирований было создано")
                .register(registry);
    }

    @Bean
    public Counter rabbitMessagesSentCounter(MeterRegistry registry) {
        return Counter.builder("app_rabbitmq_messages_sent_total")
                .description("Сколько сообщений отправлено в RabbitMQ")
                .register(registry);
    }
}
