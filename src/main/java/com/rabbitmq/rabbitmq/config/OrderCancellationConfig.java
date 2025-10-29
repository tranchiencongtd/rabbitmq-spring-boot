package com.rabbitmq.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình RabbitMQ cho tự động hủy đơn hàng chưa thanh toán
 * Sử dụng Dead Letter Exchange (DLX) và TTL
 */
@Configuration
public class OrderCancellationConfig {

    // Tên exchange và queue
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_PENDING_QUEUE = "order.pending.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";
    public static final String ORDER_PAID_QUEUE = "order.paid.queue";
    
    // Routing keys
    public static final String ROUTING_KEY_PENDING = "order.pending";
    public static final String ROUTING_KEY_CANCELLED = "order.cancelled";
    public static final String ROUTING_KEY_PAID = "order.paid";
    
    // Dead Letter Exchange
    public static final String DLX_EXCHANGE = "order.dlx.exchange";
    
    // TTL (Time To Live) - 15 phút (900000 ms)
    // Có thể điều chỉnh theo yêu cầu: 5 phút = 300000, 10 phút = 600000
    // Để TEST: 30 giây = 30000, 1 phút = 60000
    public static final int ORDER_TTL = 900000; // 30 giây (ĐỂ TEST - Đổi lại 900000 cho production)

    /**
     * Message converter để chuyển đổi object thành JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Exchange chính để xử lý đơn hàng
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    /**
     * Dead Letter Exchange - nhận message khi hết TTL
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    /**
     * Queue chứa đơn hàng đang chờ thanh toán
     * - Có TTL: message sẽ tự động expire sau thời gian cấu hình
     * - Có DLX: message expire sẽ được gửi đến Dead Letter Exchange
     */
    @Bean
    public Queue orderPendingQueue() {
        Map<String, Object> args = new HashMap<>();
        // Thiết lập TTL cho message trong queue
        args.put("x-message-ttl", ORDER_TTL);
        // Thiết lập Dead Letter Exchange
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        // Routing key khi message chuyển sang DLX
        args.put("x-dead-letter-routing-key", ROUTING_KEY_CANCELLED);
        
        return new Queue(ORDER_PENDING_QUEUE, true, false, false, args);
    }

    /**
     * Queue chứa đơn hàng bị hủy (tự động hoặc thủ công)
     */
    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(ORDER_CANCELLED_QUEUE, true);
    }

    /**
     * Queue chứa đơn hàng đã thanh toán
     */
    @Bean
    public Queue orderPaidQueue() {
        return new Queue(ORDER_PAID_QUEUE, true);
    }

    /**
     * Binding: order.pending.queue <- order.exchange (routing key: order.pending)
     */
    @Bean
    public Binding bindingPending() {
        return BindingBuilder
                .bind(orderPendingQueue())
                .to(orderExchange())
                .with(ROUTING_KEY_PENDING);
    }

    /**
     * Binding: order.cancelled.queue <- order.dlx.exchange (routing key: order.cancelled)
     * Message expire từ pending queue sẽ được route đến đây
     */
    @Bean
    public Binding bindingCancelled() {
        return BindingBuilder
                .bind(orderCancelledQueue())
                .to(dlxExchange())
                .with(ROUTING_KEY_CANCELLED);
    }

    /**
     * Binding: order.paid.queue <- order.exchange (routing key: order.paid)
     */
    @Bean
    public Binding bindingPaid() {
        return BindingBuilder
                .bind(orderPaidQueue())
                .to(orderExchange())
                .with(ROUTING_KEY_PAID);
    }
}
