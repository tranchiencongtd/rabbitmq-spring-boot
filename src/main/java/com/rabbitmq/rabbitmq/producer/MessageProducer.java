package com.rabbitmq.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendDirect(String routingKey, String msg) {
        rabbitTemplate.convertAndSend("direct-exchange", routingKey, msg);
    }

    public void sendFanout(String msg) {
        rabbitTemplate.convertAndSend("fanout-exchange", "", msg);
    }

    public void sendTopic(String routingKey, String msg) {
        rabbitTemplate.convertAndSend("topic-exchange", routingKey, msg);
    }

    public void sendHeaders(String msg, Map<String, String> headers) {
        rabbitTemplate.convertAndSend("headers-exchange", "", msg, m -> {
            if (headers != null) {
                headers.forEach((k, v) -> m.getMessageProperties().getHeaders().put(k, v));
            }
            return m;
        });
    }
}