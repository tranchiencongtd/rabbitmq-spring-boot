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
     * ⚠️ KHÔNG CẦN CONSUMER cho pending queue!
     * 
     * LÝ DO:
     * - Message cần NẰM TRONG pending queue trong 15 phút
     * - Nếu có consumer, message sẽ bị ACK và XÓA ngay lập tức
     * - Sau 15 phút, RabbitMQ tự động chuyển message sang cancelled queue (qua DLX)
     * 
     * LƯU Ý:
     * - Đơn hàng đã được lưu vào OrderService khi tạo (trong Controller)
     * - Không cần lưu lại ở đây
     */
    
    // ❌ COMMENT consumer này để message có thể expire sau 15 phút
    // @RabbitListener(queues = OrderCancellationConfig.ORDER_PENDING_QUEUE)
    // public void handlePendingOrder(Order order) {
    //     System.out.println("\n⏳ Nhận đơn hàng PENDING: " + order);
    //     System.out.println("⏰ Đơn hàng sẽ tự động hủy sau " + (OrderCancellationConfig.ORDER_TTL / 60000) + " phút nếu không thanh toán");
    //     
    //     // Lưu đơn hàng vào database/memory
    //     orderService.saveOrder(order);
    //     
    //     // Ở đây có thể gửi email/SMS thông báo cho khách hàng
    //     System.out.println("📧 Đã gửi email nhắc nhở thanh toán cho khách hàng: " + order.getUserId());
    // }

    /**
     * Xử lý đơn hàng bị hủy (tự động hoặc thủ công)
     * Message từ DLX sẽ được route đến đây khi hết TTL
     */
    @RabbitListener(queues = OrderCancellationConfig.ORDER_CANCELLED_QUEUE)
    public void handleCancelledOrder(Order order) {
        System.out.println("\n❌ =================================");
        System.out.println("❌ Nhận message HỦY đơn: " + order.getOrderId());
        
        // Lấy thông tin đơn hàng hiện tại từ service
        Order existingOrder = orderService.getOrder(order.getOrderId());
        
        if (existingOrder == null) {
            System.out.println("⚠️ KHÔNG TÌM THẤY đơn hàng: " + order.getOrderId());
            System.out.println("❌ =================================\n");
            return;
        }
        
        System.out.println("📊 Trạng thái hiện tại: " + existingOrder.getStatus());
        
        // Kiểm tra xem đơn hàng đã thanh toán chưa
        if ("PAID".equals(existingOrder.getStatus())) {
            System.out.println("✅ Đơn hàng ĐÃ THANH TOÁN trước khi hết hạn");
            System.out.println("✅ BỎ QUA việc hủy đơn hàng này");
            System.out.println("❌ =================================\n");
            return;
        }
        
        // Kiểm tra xem đã bị hủy thủ công chưa
        if ("CANCELLED".equals(existingOrder.getStatus())) {
            System.out.println("⚠️ Đơn hàng ĐÃ BỊ HỦY thủ công trước đó");
            System.out.println("⚠️ BỎ QUA việc xử lý lại");
            System.out.println("❌ =================================\n");
            return;
        }
        
        // Cập nhật trạng thái thành CANCELLED
        existingOrder.setStatus("CANCELLED");
        existingOrder.setCancelledAt(LocalDateTime.now());
        existingOrder.setCancelReason("Hủy tự động do quá thời gian thanh toán (TTL 15 phút)");
        
        // Lưu lại vào service
        orderService.saveOrder(existingOrder);
        
        System.out.println("🔄 ĐÃ CẬP NHẬT trạng thái: " + existingOrder.getOrderId() + " → CANCELLED");
        System.out.println("⏰ Thời gian hủy: " + existingOrder.getCancelledAt());
        System.out.println("📝 Lý do: " + existingOrder.getCancelReason());
        System.out.println("💼 Hoàn trả hàng về kho (nếu có)");
        System.out.println("📧 Gửi email thông báo hủy đơn cho khách: " + existingOrder.getUserId());
        System.out.println("❌ =================================\n");
        
        // Các tác vụ khác:
        // - Hoàn trả số lượng hàng vào kho
        // - Ghi log vào database
        // - Gửi thông báo cho khách hàng
        // - Cập nhật thống kê
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
