# RabbitMQ - Tự động hủy đơn hàng chưa thanh toán

## 📋 Mô tả

Ứng dụng Spring Boot sử dụng RabbitMQ để tự động hủy đơn hàng chưa thanh toán sau một khoảng thời gian nhất định (mặc định 15 phút).

## 🏗️ Kiến trúc

### Cơ chế hoạt động:

1. **Tạo đơn hàng mới**: 
   - Đơn hàng được tạo và gửi vào `order.pending.queue`
   - Queue này có TTL (Time To Live) = 15 phút

2. **Thanh toán đơn hàng**:
   - Nếu khách hàng thanh toán trong 15 phút → Đơn hàng chuyển sang trạng thái PAID
   - Message được gửi đến `order.paid.queue`

3. **Tự động hủy**:
   - Nếu sau 15 phút không thanh toán → Message tự động expire
   - Message expire được chuyển đến Dead Letter Exchange (DLX)
   - DLX route message đến `order.cancelled.queue`
   - Consumer xử lý hủy đơn hàng

### Các Queue:

- **order.pending.queue**: Chứa đơn hàng đang chờ thanh toán (có TTL)
- **order.paid.queue**: Chứa đơn hàng đã thanh toán
- **order.cancelled.queue**: Chứa đơn hàng bị hủy (tự động hoặc thủ công)

### Exchange:

- **order.exchange**: Direct Exchange chính
- **order.dlx.exchange**: Dead Letter Exchange để nhận message expire

## 🚀 Cách chạy ứng dụng

### 1. Khởi động RabbitMQ

```bash
# Sử dụng Docker Compose
docker-compose up -d

# Hoặc chạy RabbitMQ trực tiếp
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 2. Truy cập RabbitMQ Management Console

- URL: http://localhost:15672
- Username: `guest`
- Password: `guest`

### 3. Chạy ứng dụng Spring Boot

```bash
# Sử dụng Maven
./mvnw spring-boot:run

# Hoặc trên Windows
mvnw.cmd spring-boot:run
```

## 📝 API Endpoints

### 1. Tạo đơn hàng mới

```bash
POST http://localhost:8080/api/orders/create
Content-Type: application/json

{
  "userId": "user123",
  "amount": 500000
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đơn hàng đã được tạo và đang chờ thanh toán",
  "orderId": "ORD-A1B2C3D4",
  "ttl": "Đơn hàng sẽ tự động hủy sau 15 phút nếu không thanh toán"
}
```

### 2. Thanh toán đơn hàng

```bash
POST http://localhost:8080/api/orders/{orderId}/pay
```

**Response:**
```json
{
  "success": true,
  "message": "Thanh toán đơn hàng thành công",
  "orderId": "ORD-A1B2C3D4"
}
```

### 3. Hủy đơn hàng (thủ công)

```bash
POST http://localhost:8080/api/orders/{orderId}/cancel
```

**Response:**
```json
{
  "success": true,
  "message": "Đã hủy đơn hàng thành công",
  "orderId": "ORD-A1B2C3D4"
}
```

### 4. Lấy thông tin đơn hàng

```bash
GET http://localhost:8080/api/orders/{orderId}
```

### 5. Lấy danh sách tất cả đơn hàng

```bash
GET http://localhost:8080/api/orders
```

## 🧪 Test Scenarios

### Scenario 1: Tự động hủy đơn hàng

1. Tạo đơn hàng mới
2. Chờ 15 phút (hoặc thời gian TTL đã cấu hình)
3. Đơn hàng sẽ tự động chuyển sang trạng thái CANCELLED

### Scenario 2: Thanh toán đơn hàng thành công

1. Tạo đơn hàng mới
2. Thanh toán trong vòng 15 phút
3. Đơn hàng chuyển sang trạng thái PAID
4. Message expire sau đó sẽ bị bỏ qua

### Scenario 3: Hủy đơn hàng thủ công

1. Tạo đơn hàng mới
2. Gọi API hủy đơn hàng
3. Đơn hàng chuyển sang trạng thái CANCELLED ngay lập tức

## ⚙️ Cấu hình TTL

Mặc định TTL = 15 phút (900000 ms). Để thay đổi:

```java
// File: OrderCancellationConfig.java
public static final int ORDER_TTL = 300000; // 5 phút
// hoặc
public static final int ORDER_TTL = 600000; // 10 phút
// hoặc
public static final int ORDER_TTL = 30000;  // 30 giây (để test)
```

## 📊 Monitoring

### Xem log trong console:

- ⏳ Đơn hàng đang chờ: `Nhận đơn hàng PENDING`
- ✅ Đơn hàng đã thanh toán: `Đơn hàng đã THANH TOÁN`
- ❌ Đơn hàng bị hủy: `Đơn hàng bị HỦY`

### Xem trong RabbitMQ Management:

1. Vào tab **Queues**
2. Xem số lượng message trong mỗi queue:
   - `order.pending.queue`: Đơn hàng đang chờ
   - `order.paid.queue`: Đơn hàng đã thanh toán
   - `order.cancelled.queue`: Đơn hàng bị hủy

## 🔧 Cải tiến trong Production

1. **Lưu trữ Database**: 
   - Thay thế `ConcurrentHashMap` bằng database (MySQL, PostgreSQL)
   - Sử dụng JPA/Hibernate

2. **Gửi thông báo**:
   - Email: Sử dụng Spring Mail
   - SMS: Tích hợp với Twilio, AWS SNS
   - Push Notification

3. **Retry mechanism**:
   - Xử lý lại message khi có lỗi
   - Dead Letter Queue cho error handling

4. **Logging & Monitoring**:
   - ELK Stack (Elasticsearch, Logstash, Kibana)
   - Prometheus + Grafana
   - Spring Boot Actuator

5. **Caching**:
   - Redis để cache trạng thái đơn hàng
   - Giảm tải cho database

## 📁 Cấu trúc thư mục

```
src/main/java/com/rabbitmq/rabbitmq/
├── config/
│   ├── RabbitConfig.java              # Cấu hình các loại exchange
│   └── OrderCancellationConfig.java   # Cấu hình cho order cancellation
├── controller/
│   ├── TestController.java            # Controller test ban đầu
│   └── OrderController.java           # REST API cho orders
├── consumer/
│   ├── MessageConsumer.java           # Consumer test ban đầu
│   └── OrderConsumer.java             # Consumer xử lý orders
├── producer/
│   ├── MessageProducer.java           # Producer test ban đầu
│   └── OrderProducer.java             # Producer gửi orders
├── service/
│   └── OrderService.java              # Service quản lý orders
└── dto/
    ├── MessageRequest.java            # DTO test ban đầu
    └── Order.java                     # DTO đơn hàng
```

## 🎯 Lợi ích của giải pháp

1. **Tự động hóa**: Không cần cronjob hay scheduled task
2. **Tin cậy**: RabbitMQ đảm bảo message không bị mất
3. **Scalable**: Dễ dàng scale horizontal với nhiều consumer
4. **Flexible**: Có thể điều chỉnh TTL theo nhu cầu
5. **Decoupled**: Tách biệt logic business khỏi việc hủy đơn hàng

## 📞 Liên hệ

Nếu có câu hỏi hoặc góp ý, vui lòng tạo issue trên GitHub.
