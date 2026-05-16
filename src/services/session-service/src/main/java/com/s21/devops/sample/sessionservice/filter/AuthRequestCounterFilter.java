package com.s21.devops.sample.sessionservice.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Считает запросы на авторизацию (любые HTTP-запросы к session-service,
 * т.к. весь сервис посвящён авторизации и регистрации пользователей).
 */
@Component
@Order(1)
public class AuthRequestCounterFilter implements Filter {

    private final Counter authRequestsCounter;

    @Autowired
    public AuthRequestCounterFilter(MeterRegistry registry) {
        this.authRequestsCounter = Counter.builder("app_auth_requests_total")
                .description("Сколько запросов на авторизацию получено")
                .register(registry);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Считаем все запросы к session-service — это и есть запросы на авторизацию
        if (request instanceof HttpServletRequest) {
            authRequestsCounter.increment();
        }
        chain.doFilter(request, response);
    }
}
