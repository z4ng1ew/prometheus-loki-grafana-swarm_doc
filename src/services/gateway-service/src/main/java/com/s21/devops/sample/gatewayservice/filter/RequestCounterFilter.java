package com.s21.devops.sample.gatewayservice.filter;

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
import java.io.IOException;

/**
 * Считает все HTTP-запросы, проходящие через gateway-service.
 *
 * @Component — Spring увидит класс и сам создаст из него объект.
 * @Order(1)  — выполнить ПЕРВЫМ в цепочке фильтров (раньше Security).
 * Filter    — интерфейс из javax.servlet, через который проходит каждый HTTP-запрос
 *              ДО того, как попадёт в контроллер.
 */
@Component
@Order(1)
public class RequestCounterFilter implements Filter {

    private final Counter requestsCounter;

    @Autowired
    public RequestCounterFilter(MeterRegistry registry) {
        this.requestsCounter = Counter.builder("app_gateway_requests_total")
                .description("Сколько запросов получено на gateway")
                .register(registry);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        requestsCounter.increment();
        chain.doFilter(request, response);
    }
}
