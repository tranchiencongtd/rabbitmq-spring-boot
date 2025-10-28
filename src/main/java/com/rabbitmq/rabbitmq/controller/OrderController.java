package com.rabbitmq.rabbitmq.controller;

import com.rabbitmq.rabbitmq.dto.CreateOrderRequest;
import com.rabbitmq.rabbitmq.dto.Order;
import com.rabbitmq.rabbitmq.producer.OrderProducer;
import com.rabbitmq.rabbitmq.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST API để quản lý đơn hàng
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderProducer orderProducer;
    private final OrderService orderService;

    public OrderController(OrderProducer orderProducer, OrderService orderService) {
        this.orderProducer = orderProducer;
        this.orderService = orderService;
    }

    /**
     * Tạo đơn hàng mới
     * POST /api/orders/create
     * Body: {
     *   "userId": "user123",
     *   "amount": 500000
     * }
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        String userId = request.getUserId();
        Double amount = request.getAmount();
        
        // Tạo đơn hàng mới
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, userId, amount);
        
        // Gửi vào queue pending
        orderProducer.sendOrderToPending(order);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đơn hàng đã được tạo và đang chờ thanh toán",
                "orderId", orderId,
                "ttl", "Đơn hàng sẽ tự động hủy sau 15 phút nếu không thanh toán"
        ));
    }

    /**
     * Thanh toán đơn hàng
     * POST /api/orders/{orderId}/pay
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy đơn hàng"
            ));
        }
        
        if ("CANCELLED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đơn hàng đã bị hủy, không thể thanh toán"
            ));
        }
        
        if ("PAID".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đơn hàng đã được thanh toán trước đó"
            ));
        }
        
        // Cập nhật trạng thái và gửi vào queue paid
        order.setStatus("PAID");
        orderProducer.sendOrderToPaid(order);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Thanh toán đơn hàng thành công",
                "orderId", orderId
        ));
    }

    /**
     * Hủy đơn hàng thủ công
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy đơn hàng"
            ));
        }
        
        if ("CANCELLED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Đơn hàng đã bị hủy trước đó"
            ));
        }
        
        if ("PAID".equals(order.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không thể hủy đơn hàng đã thanh toán"
            ));
        }
        
        // Cập nhật trạng thái và gửi vào queue cancelled
        order.setStatus("CANCELLED");
        order.setCancelReason("Hủy bởi người dùng");
        orderProducer.sendOrderToCancelled(order);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã hủy đơn hàng thành công",
                "orderId", orderId
        ));
    }

    /**
     * Lấy thông tin đơn hàng
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        
        if (order == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy đơn hàng"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "order", order
        ));
    }

    /**
     * Lấy danh sách tất cả đơn hàng
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        Map<String, Order> orders = orderService.getAllOrders();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", orders.size(),
                "orders", orders.values()
        ));
    }
}
