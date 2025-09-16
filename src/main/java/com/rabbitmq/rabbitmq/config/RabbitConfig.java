package com.rabbitmq.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    // ===== Direct Exchange =====
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange("direct-exchange");
    }

    @Bean
    Queue directQueue() {
        return new Queue("direct-queue", false);
    }

    @Bean
    Binding directBinding(Queue directQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(directQueue).to(directExchange).with("direct-key");
    }

    // ===== Fanout Exchange =====
    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout-exchange");
    }

    @Bean
    Queue fanoutQueue1() {
        return new Queue("fanout-queue-1", false);
    }

    @Bean
    Queue fanoutQueue2() {
        return new Queue("fanout-queue-2", false);
    }

    @Bean
    Binding fanoutBinding1(Queue fanoutQueue1, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutQueue1).to(fanoutExchange);
    }

    @Bean
    Binding fanoutBinding2(Queue fanoutQueue2, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutQueue2).to(fanoutExchange);
    }

    // ===== Topic Exchange =====
    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange("topic-exchange");
    }

    @Bean
    Queue topicQueue() {
        return new Queue("topic-queue", false);
    }

    @Bean
    Binding topicBinding(Queue topicQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(topicQueue).to(topicExchange).with("topic.*");
    }

    // ===== Headers Exchange =====
    @Bean
    HeadersExchange headersExchange() {
        return new HeadersExchange("headers-exchange");
    }

    @Bean
    Queue headersQueue() {
        return new Queue("headers-queue", false);
    }

    @Bean
    Binding headersBinding(Queue headersQueue, HeadersExchange headersExchange) {
        return BindingBuilder.bind(headersQueue)
                .to(headersExchange)
                .whereAll(Map.of("format", "pdf", "type", "report"))
                .match();
    }
}
