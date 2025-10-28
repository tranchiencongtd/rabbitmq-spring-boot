# HƯỚNG DẪN NHANH - TỰ ĐỘNG HỦY ĐƠN HÀNG

## 🚀 Chạy ứng dụng trong 3 bước

### Bước 1: Khởi động RabbitMQ
```powershell
docker-compose up -d
```

### Bước 2: Chạy Spring Boot
```powershell
./mvnw spring-boot:run
```

### Bước 3: Test API

#### 3.1. Tạo đơn hàng (PowerShell)
```powershell
$response = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create -ContentType 'application/json' -Body '{"userId":"user123","amount":500000}'
$orderId = $response.orderId
Write-Host "✅ Đã tạo đơn hàng: $orderId"
```

#### 3.2. Xem đơn hàng vừa tạo
```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/orders/$orderId"
```

#### 3.3. Thanh toán đơn hàng (trong vòng 15 phút)
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/orders/$orderId/pay"
```

#### 3.4. Hoặc hủy đơn hàng thủ công
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/orders/$orderId/cancel"
```

## 🧪 Test tự động hủy

### Test nhanh (TTL = 30 giây):

1. **Sửa file OrderCancellationConfig.java**:
```java
public static final int ORDER_TTL = 30000; // 30 giây
```

2. **Restart ứng dụng**

3. **Tạo đơn hàng và đợi 30 giây**:
```powershell
# Tạo đơn hàng
$response = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create -ContentType 'application/json' -Body '{"userId":"user123","amount":500000}'
$orderId = $response.orderId
Write-Host "Đợi 30 giây..."

# Đợi 30 giây
Start-Sleep -Seconds 30

# Kiểm tra trạng thái
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/orders/$orderId"
```

4. **Xem log trong console** - Sẽ thấy message "❌ Đơn hàng bị HỦY"

## 📊 Monitoring

### RabbitMQ Management Console
- URL: http://localhost:15672
- User/Pass: guest/guest
- Vào tab **Queues** để xem:
  - `order.pending.queue` - Đơn đang chờ
  - `order.paid.queue` - Đơn đã thanh toán  
  - `order.cancelled.queue` - Đơn bị hủy

### Console Log
Theo dõi log trong terminal:
- 📤 Gửi đơn vào queue
- ⏳ Nhận đơn hàng PENDING
- ✅ Đơn hàng đã THANH TOÁN
- ❌ Đơn hàng bị HỦY

## 🎯 Các tình huống

### ✅ Tình huống 1: Khách hàng thanh toán đúng hạn
```powershell
# Tạo đơn
$r1 = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create -ContentType 'application/json' -Body '{"userId":"user001","amount":300000}'

# Thanh toán ngay
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/orders/$($r1.orderId)/pay"

# Kết quả: Đơn hàng = PAID, không bị hủy
```

### ❌ Tình huống 2: Khách hàng không thanh toán
```powershell
# Tạo đơn
$r2 = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create -ContentType 'application/json' -Body '{"userId":"user002","amount":500000}'

# Không làm gì, đợi 15 phút
# Kết quả: Đơn hàng tự động = CANCELLED
```

### 🔄 Tình huống 3: Khách hàng hủy đơn
```powershell
# Tạo đơn
$r3 = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create -ContentType 'application/json' -Body '{"userId":"user003","amount":750000}'

# Hủy ngay
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/orders/$($r3.orderId)/cancel"

# Kết quả: Đơn hàng = CANCELLED ngay lập tức
```

## 💡 Tips

### Tạo nhiều đơn hàng để test
```powershell
1..5 | ForEach-Object {
    $body = @{
        userId = "user$_"
        amount = Get-Random -Minimum 100000 -Maximum 1000000
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/orders/create -ContentType 'application/json' -Body $body
    Write-Host "Tạo đơn: $($response.orderId)"
}
```

### Xem tất cả đơn hàng
```powershell
$orders = Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/orders
$orders.orders | Format-Table orderId, userId, amount, status, createdAt
```

## ❓ Troubleshooting

### Lỗi kết nối RabbitMQ
```
Failed to connect to RabbitMQ
```
**Giải pháp**: Kiểm tra RabbitMQ đã chạy chưa
```powershell
docker ps | Select-String rabbitmq
```

### Port 8080 đã được sử dụng
**Giải pháp**: Thay đổi port trong application.properties
```properties
server.port=8081
```

### Đơn hàng không tự động hủy
**Giải pháp**: 
1. Kiểm tra TTL trong OrderCancellationConfig.java
2. Xem log RabbitMQ Management Console
3. Restart lại ứng dụng sau khi thay đổi config

## 📚 Tài liệu chi tiết

Xem file `README_ORDER_CANCELLATION.md` để biết thêm chi tiết về:
- Kiến trúc hệ thống
- Dead Letter Exchange (DLX)
- Message TTL
- Cải tiến cho production
