package com.s21.devops.sample.bookingservice.Statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s21.devops.sample.bookingservice.Communication.BookingStatisticsMessage;
import io.micrometer.core.instrument.Counter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueueProducer {
    @Value("${fanout.exchange}")
    private String fanoutExchange;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier("rabbitMessagesSentCounter")
    private Counter rabbitMessagesSentCounter;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void putStatistics(BookingStatisticsMessage bookingStatisticsMessage) throws JsonProcessingException {
        System.out.println("Sending message...");
        rabbitTemplate.setExchange(fanoutExchange);
        rabbitTemplate.convertAndSend(objectMapper.writeValueAsString(bookingStatisticsMessage));
        rabbitMessagesSentCounter.increment();
        System.out.println("Message was sent successfully!");
    }
}
