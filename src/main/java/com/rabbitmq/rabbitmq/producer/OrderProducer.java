package com.rabbitmq.rabbitmq.producer;

import com.rabbitmq.rabbitmq.config.OrderCancellationConfig;
import com.rabbitmq.rabbitmq.dto.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Producer gửi message đơn hàng vào RabbitMQ
 */
@Service
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Gửi đơn hàng mới vào queue pending
     * Đơn hàng sẽ tự động expire sau TTL nếu không được thanh toán
     */
    public void sendOrderToPending(Order order) {
        System.out.println("📤 Gửi đơn hàng vào queue pending: " + order.getOrderId());
        rabbitTemplate.convertAndSend(
                OrderCancellationConfig.ORDER_EXCHANGE,
                OrderCancellationConfig.ROUTING_KEY_PENDING,
                order
        );
    }

    /**
     * Gửi thông tin đơn hàng đã thanh toán
     */
    public void sendOrderToPaid(Order order) {
        System.out.println("💳 Gửi đơn hàng đã thanh toán: " + order.getOrderId());
        rabbitTemplate.convertAndSend(
                OrderCancellationConfig.ORDER_EXCHANGE,
                OrderCancellationConfig.ROUTING_KEY_PAID,
                order
        );
    }

    /**
     * Gửi thông tin đơn hàng bị hủy thủ công
     */
    public void sendOrderToCancelled(Order order) {
        System.out.println("❌ Gửi đơn hàng bị hủy: " + order.getOrderId());
        rabbitTemplate.convertAndSend(
                OrderCancellationConfig.ORDER_EXCHANGE,
                OrderCancellationConfig.ROUTING_KEY_CANCELLED,
                order
        );
    }
}
