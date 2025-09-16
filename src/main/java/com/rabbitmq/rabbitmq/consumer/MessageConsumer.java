package com.rabbitmq.rabbitmq.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {
    @RabbitListener(queues = "direct-queue")
    public void receiveDirect(String msg) {
        System.out.println("Direct Received: " + msg);
    }

    @RabbitListener(queues = "fanout-queue-1")
    public void receiveFanout1(String msg) {
        System.out.println("Fanout1 Received: " + msg);
    }

    @RabbitListener(queues = "fanout-queue-2")
    public void receiveFanout2(String msg) {
        System.out.println("Fanout2 Received: " + msg);
    }

    @RabbitListener(queues = "topic-queue")
    public void receiveTopic(String msg) {
        System.out.println("Topic Received: " + msg);
    }

    @RabbitListener(queues = "headers-queue")
    public void receiveHeaders(String msg) {
        System.out.println("Headers Received: " + msg);
    }
}
