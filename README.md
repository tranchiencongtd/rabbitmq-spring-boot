# 🐰 RabbitMQ - Tự động hủy đơn hàng chưa thanh toán

## 📖 Giới thiệu

Ứng dụng Spring Boot demo việc sử dụng RabbitMQ để **tự động hủy đơn hàng** sau khoảng thời gian nhất định nếu khách hàng không thanh toán.

## 🎨 Demo Web UI

**Giao diện web đẹp mắt với real-time updates!**

![Demo UI](https://img.shields.io/badge/Demo-Live-success?style=for-the-badge)

Truy cập: **http://localhost:8080** sau khi chạy ứng dụng để xem giao diện demo đầy đủ tính năng.

### 🎯 Bài toán

Trong thương mại điện tử, khi khách hàng đặt hàng nhưng không thanh toán, hàng bị giữ trong kho gây lãng phí. Cần có cơ chế **tự động hủy đơn hàng** sau một khoảng thời gian (ví dụ: 15 phút) để:
- Hoàn trả hàng về kho
- Cho phép khách khác mua
- Gửi thông báo cho khách hàng

### 💡 Giải pháp

Sử dụng **RabbitMQ** với:
- **TTL (Time To Live)**: Thời gian sống của message trong queue
- **Dead Letter Exchange (DLX)**: Nơi nhận message khi hết TTL
- **Automatic Routing**: Tự động route message expire đến queue xử lý hủy đơn

## 🚀 Bắt đầu nhanh

### 1️⃣ Khởi động RabbitMQ

```powershell
docker-compose up -d
```

### 2️⃣ Chạy ứng dụng

```powershell
./mvnw spring-boot:run
```

### 3️⃣ Mở trình duyệt

**🎨 Giao diện Web Demo:**
```
http://localhost:8080
```

**🐰 RabbitMQ Management:**
```
http://localhost:15672
Username: guest
Password: guest
```

### 4️⃣ (Tùy chọn) Test API thủ công

```powershell
# Chạy script test tự động
./test_order_cancellation.ps1

# Hoặc test thủ công
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create `
  -ContentType 'application/json' `
  -Body '{"userId":"user123","amount":500000}'
```

## 📚 Tài liệu

| File | Mô tả |
|------|-------|
| **[WEB_UI_GUIDE.md](WEB_UI_GUIDE.md)** | 🎨 **Hướng dẫn sử dụng giao diện web demo** |
| **[diagrams/README.md](diagrams/README.md)** | 📊 **7 Diagrams chuyên nghiệp (Mermaid format)** |
| [QUICKSTART.md](QUICKSTART.md) | 🏃 Hướng dẫn chạy nhanh với các command cụ thể |
| [README_ORDER_CANCELLATION.md](README_ORDER_CANCELLATION.md) | 📖 Tài liệu chi tiết về kiến trúc và API |
| [FLOW_DIAGRAM.md](FLOW_DIAGRAM.md) | 🔄 Sơ đồ luồng xử lý chi tiết |
| [VISUALIZATION.md](VISUALIZATION.md) | 📊 Trực quan hóa toàn bộ hệ thống |
| [FILE_SUMMARY.md](FILE_SUMMARY.md) | 📁 Tổng hợp tất cả các file đã tạo |

## 🎯 Tính năng

- ✅ Tạo đơn hàng mới
- ✅ Thanh toán đơn hàng
- ✅ Hủy đơn hàng thủ công
- ✅ **Tự động hủy đơn hàng** sau 15 phút (có thể cấu hình)
- ✅ Xem thông tin đơn hàng
- ✅ Xem danh sách tất cả đơn hàng

## 🏗️ Kiến trúc

```
Client → REST API → Producer → RabbitMQ → Consumer → Service
                                  ├─ pending queue (TTL)
                                  ├─ paid queue
                                  └─ cancelled queue (từ DLX)
```

## 📡 API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/orders/create` | Tạo đơn hàng mới |
| POST | `/api/orders/{id}/pay` | Thanh toán đơn hàng |
| POST | `/api/orders/{id}/cancel` | Hủy đơn hàng thủ công |
| GET | `/api/orders/{id}` | Xem chi tiết đơn hàng |
| GET | `/api/orders` | Xem tất cả đơn hàng |

## 🧪 Test Scenarios

### ✅ Scenario 1: Thanh toán thành công
```powershell
# Tạo đơn
$order = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create `
  -ContentType 'application/json' -Body '{"userId":"user123","amount":500000}'

# Thanh toán ngay
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/orders/$($order.orderId)/pay"
```

### ❌ Scenario 2: Tự động hủy
```powershell
# Tạo đơn
$order = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create `
  -ContentType 'application/json' -Body '{"userId":"user456","amount":750000}"

# Chờ 15 phút... (hoặc 30 giây nếu đã giảm TTL)
# Đơn hàng sẽ TỰ ĐỘNG bị hủy
```

## ⚙️ Cấu hình

### TTL (Time To Live)

File: `src/main/java/com/rabbitmq/rabbitmq/config/OrderCancellationConfig.java`

```java
// Mặc định: 15 phút
public static final int ORDER_TTL = 900000;

// Để test nhanh: 30 giây
public static final int ORDER_TTL = 30000;
```

## 📊 Monitoring

### RabbitMQ Management Console
- URL: http://localhost:15672
- Username/Password: `guest` / `guest`

### Application Logs
```
📤 Gửi đơn hàng vào queue pending: ORD-ABC123
⏳ Nhận đơn hàng PENDING: Order{...}
✅ Đơn hàng đã THANH TOÁN: Order{...}
❌ Đơn hàng bị HỦY: Order{...}
```

## 🛠️ Công nghệ sử dụng

- **Spring Boot 3.5.5** - Framework chính
- **Spring AMQP** - Tích hợp RabbitMQ
- **RabbitMQ 4** - Message broker
- **Docker Compose** - Container orchestration
- **Jackson** - JSON serialization
- **Maven** - Build tool

## 📁 Cấu trúc dự án

```
src/main/java/com/rabbitmq/rabbitmq/
├── config/
│   ├── OrderCancellationConfig.java    # Cấu hình queues, exchanges, DLX
│   └── RabbitConfig.java               # Cấu hình test
├── controller/
│   └── OrderController.java            # REST API endpoints
├── producer/
│   └── OrderProducer.java              # Gửi message
├── consumer/
│   └── OrderConsumer.java              # Nhận và xử lý message
├── service/
│   └── OrderService.java               # Business logic
└── dto/
    ├── Order.java                      # Model đơn hàng
    └── CreateOrderRequest.java         # DTO request
```

## 🎓 Học được gì từ project này?

1. **RabbitMQ Basics**
   - Exchanges, Queues, Bindings
   - Routing keys
   - Message serialization

2. **Advanced RabbitMQ**
   - Dead Letter Exchange (DLX)
   - Message TTL
   - Automatic message routing

3. **Spring AMQP**
   - RabbitTemplate
   - @RabbitListener
   - Message converter

4. **System Design**
   - Event-driven architecture
   - Asynchronous processing
   - Decoupled systems

## 🔧 Troubleshooting

### Lỗi: Connection refused
```
Giải pháp: Kiểm tra RabbitMQ đã chạy
docker ps | Select-String rabbitmq
```

### Đơn hàng không tự động hủy
```
Giải pháp: 
1. Kiểm tra TTL trong OrderCancellationConfig
2. Xem RabbitMQ Management Console
3. Restart ứng dụng
```

## 🚀 Production Checklist

- [ ] Tích hợp database (JPA/Hibernate)
- [ ] Email/SMS notification service
- [ ] Redis caching
- [ ] Authentication & Authorization
- [ ] Rate limiting
- [ ] Monitoring & Alerting (Prometheus, Grafana)
- [ ] Distributed tracing (Zipkin, Jaeger)
- [ ] Retry mechanism & Error handling
- [ ] Load balancing
- [ ] CI/CD pipeline

## 🤝 Đóng góp

Pull requests được chào đón! Đối với thay đổi lớn, vui lòng mở issue trước để thảo luận.

## 📄 License

[MIT](LICENSE)

## 👨‍💻 Tác giả

Created with ❤️ by GitHub Copilot

---

**⭐ Nếu project này hữu ích, hãy cho một star!**

**📧 Liên hệ:** [GitHub Repository](https://github.com/tranchiencongtd/rabbitmq-spring-boot)
