package com.rabbitmq.rabbitmq.controller;

import com.rabbitmq.rabbitmq.dto.MessageRequest;
import com.rabbitmq.rabbitmq.producer.MessageProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/send")
public class TestController {

    private final MessageProducer producer;

    public TestController(MessageProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public String sendMessage(@RequestBody MessageRequest request) {
        switch (request.getExchangeType().toLowerCase()) {
            case "direct":
                producer.sendDirect(request.getRoutingKey(), request.getMessage());
                break;
            case "fanout":
                producer.sendFanout(request.getMessage());
                break;
            case "topic":
                producer.sendTopic(request.getRoutingKey(), request.getMessage());
                break;
            case "headers":
                producer.sendHeaders(request.getMessage(), request.getHeaders());
                break;
            default:
                return "Exchange type không hợp lệ!";
        }
        return "Sent to " + request.getExchangeType() + ": " + request.getMessage();
    }
}
