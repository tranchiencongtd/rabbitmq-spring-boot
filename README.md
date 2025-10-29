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

**⭐ Nếu project này hữu ích, hãy cho một star!**

**📧 Liên hệ:** [GitHub Repository](https://github.com/tranchiencongtd/rabbitmq-spring-boot)
