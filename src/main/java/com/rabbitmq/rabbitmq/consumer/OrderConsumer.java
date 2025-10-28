package com.rabbitmq.rabbitmq.consumer;

import com.rabbitmq.rabbitmq.config.OrderCancellationConfig;
import com.rabbitmq.rabbitmq.dto.Order;
import com.rabbitmq.rabbitmq.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Consumer xử lý các message đơn hàng
 */
@Service
public class OrderConsumer {

    private final OrderService orderService;

    public OrderConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Xử lý đơn hàng đang chờ thanh toán
     * Consumer này nhận message từ pending queue
     */
    @RabbitListener(queues = OrderCancellationConfig.ORDER_PENDING_QUEUE)
    public void handlePendingOrder(Order order) {
        System.out.println("\n⏳ Nhận đơn hàng PENDING: " + order);
        System.out.println("⏰ Đơn hàng sẽ tự động hủy sau " + (OrderCancellationConfig.ORDER_TTL / 60000) + " phút nếu không thanh toán");
        
        // Lưu đơn hàng vào database/memory
        orderService.saveOrder(order);
        
        // Ở đây có thể gửi email/SMS thông báo cho khách hàng
        System.out.println("📧 Đã gửi email nhắc nhở thanh toán cho khách hàng: " + order.getUserId());
    }

    /**
     * Xử lý đơn hàng bị hủy (tự động hoặc thủ công)
     * Message từ DLX sẽ được route đến đây khi hết TTL
     */
    @RabbitListener(queues = OrderCancellationConfig.ORDER_CANCELLED_QUEUE)
    public void handleCancelledOrder(Order order) {
        System.out.println("\n❌ Đơn hàng bị HỦY: " + order);
        
        // Cập nhật trạng thái đơn hàng
        Order existingOrder = orderService.getOrder(order.getOrderId());
        if (existingOrder != null && !"PAID".equals(existingOrder.getStatus())) {
            existingOrder.setStatus("CANCELLED");
            existingOrder.setCancelledAt(LocalDateTime.now());
            existingOrder.setCancelReason("Hủy tự động do quá thời gian thanh toán");
            
            System.out.println("🔄 Đã cập nhật trạng thái đơn hàng: " + existingOrder.getOrderId());
            System.out.println("💼 Hoàn trả hàng về kho (nếu có)");
            System.out.println("📧 Gửi email thông báo hủy đơn cho khách hàng: " + existingOrder.getUserId());
            
            // Các tác vụ khác:
            // - Hoàn trả số lượng hàng vào kho
            // - Ghi log vào database
            // - Gửi thông báo cho khách hàng
            // - Cập nhật thống kê
        } else if (existingOrder != null && "PAID".equals(existingOrder.getStatus())) {
            System.out.println("✅ Đơn hàng đã được thanh toán trước đó, bỏ qua hủy: " + order.getOrderId());
        }
    }

    /**
     * Xử lý đơn hàng đã thanh toán
     */
    @RabbitListener(queues = OrderCancellationConfig.ORDER_PAID_QUEUE)
    public void handlePaidOrder(Order order) {
        System.out.println("\n✅ Đơn hàng đã THANH TOÁN: " + order);
        
        // Cập nhật trạng thái
        Order existingOrder = orderService.getOrder(order.getOrderId());
        if (existingOrder != null) {
            existingOrder.setStatus("PAID");
            System.out.println("💰 Xác nhận thanh toán thành công");
            System.out.println("📦 Bắt đầu xử lý đóng gói và giao hàng");
            System.out.println("📧 Gửi email xác nhận đơn hàng cho khách hàng: " + existingOrder.getUserId());
            
            // Các tác vụ khác:
            // - Tạo phiếu giao hàng
            // - Gửi thông báo cho bộ phận vận chuyển
            // - Ghi log vào database
        }
    }
}
