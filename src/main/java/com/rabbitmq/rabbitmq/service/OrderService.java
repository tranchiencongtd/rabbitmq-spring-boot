package com.rabbitmq.rabbitmq.service;

import com.rabbitmq.rabbitmq.dto.Order;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý trạng thái đơn hàng
 * Trong thực tế, nên lưu vào database
 */
@Service
public class OrderService {
    
    // Lưu trữ đơn hàng trong memory (demo)
    // Trong production nên dùng database
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    /**
     * Lưu đơn hàng
     */
    public void saveOrder(Order order) {
        orders.put(order.getOrderId(), order);
    }

    /**
     * Lấy thông tin đơn hàng
     */
    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    /**
     * Kiểm tra đơn hàng có tồn tại không
     */
    public boolean orderExists(String orderId) {
        return orders.containsKey(orderId);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    public void updateOrderStatus(String orderId, String status) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus(status);
        }
    }

    /**
     * Xóa đơn hàng khỏi memory
     */
    public void removeOrder(String orderId) {
        orders.remove(orderId);
    }

    /**
     * Lấy tất cả đơn hàng
     */
    public Map<String, Order> getAllOrders() {
        return new ConcurrentHashMap<>(orders);
    }
}
