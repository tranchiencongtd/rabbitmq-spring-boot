# 🎨 HƯỚNG DẪN SỬ DỤNG GIAO DIỆN WEB DEMO

## 🚀 Khởi động ứng dụng

### 1. Chạy RabbitMQ
```powershell
docker-compose up -d
```

### 2. Chạy Spring Boot
```powershell
./mvnw spring-boot:run
```

### 3. Mở trình duyệt
Truy cập: **http://localhost:8080**

## 🎯 Tính năng giao diện

### 📊 Dashboard Overview
- **Header**: Hiển thị TTL và trạng thái kết nối
- **Panel trái**: Tạo đơn hàng và thống kê
- **Panel phải**: Danh sách đơn hàng real-time
- **Panel dưới**: Hướng dẫn sử dụng

### ✨ Các tính năng chính

#### 1️⃣ Tạo đơn hàng
- **Cách 1**: Nhập thủ công
  - Nhập mã khách hàng (ví dụ: user123)
  - Nhập số tiền (tối thiểu 1,000 VNĐ)
  - Click "Tạo đơn hàng"

- **Cách 2**: Tạo nhanh
  - Click các nút 250K, 500K, hoặc 1M
  - Hệ thống tự động tạo đơn với giá trị tương ứng

#### 2️⃣ Xem danh sách đơn hàng
- Hiển thị tất cả đơn hàng theo thời gian (mới nhất trước)
- Mỗi đơn hàng hiển thị:
  - Mã đơn hàng
  - Trạng thái (PENDING, PAID, CANCELLED)
  - Thông tin khách hàng
  - Số tiền
  - Thời gian tạo
  - **Timer đếm ngược** (đơn PENDING)

#### 3️⃣ Thao tác với đơn hàng
- **Thanh toán**: Click nút "Thanh toán" → Đơn chuyển sang PAID
- **Hủy đơn**: Click nút "Hủy đơn" → Đơn chuyển sang CANCELLED
- **Tự động hủy**: Không làm gì → Sau 15 phút tự động hủy

#### 4️⃣ Thống kê real-time
Hiển thị số lượng đơn hàng theo từng trạng thái:
- 🟡 **Đang chờ** (PENDING)
- 🟢 **Đã thanh toán** (PAID)
- 🔴 **Đã hủy** (CANCELLED)

## 🎭 Demo Scenarios

### Scenario 1: Thanh toán thành công ✅

1. **Tạo đơn hàng**
   - Click nút "500K" hoặc nhập thủ công
   - Đợi notification "Đã tạo đơn hàng ORD-XXXXX"

2. **Quan sát**
   - Đơn hàng xuất hiện trong danh sách
   - Trạng thái: PENDING (màu vàng)
   - Timer bắt đầu đếm ngược từ 15:00

3. **Thanh toán**
   - Click nút "Thanh toán" trên card đơn hàng
   - Xác nhận trong dialog
   - Timer biến mất
   - Trạng thái chuyển sang PAID (màu xanh)
   - Thống kê cập nhật

4. **Kết quả**
   - Đơn hàng vẫn trong danh sách
   - Không còn nút hành động
   - Không bị hủy sau 15 phút

### Scenario 2: Tự động hủy ⏰

1. **Tạo đơn hàng**
   - Tạo đơn hàng mới bất kỳ

2. **Quan sát timer**
   - Timer đếm ngược từ 15:00
   - Khi còn dưới 2 phút → Timer chuyển màu đỏ
   - Warning visual rõ ràng

3. **Không làm gì**
   - Đợi đến khi timer về 00:00
   - Timer hiển thị "HẾT HẠN"

4. **Kết quả (sau vài giây)**
   - Trang tự động refresh
   - Trạng thái chuyển sang CANCELLED
   - Hiển thị lý do: "Hủy tự động do quá thời gian thanh toán"
   - Thống kê cập nhật

> 💡 **Tip**: Để test nhanh, giảm TTL xuống 30 giây trong `OrderCancellationConfig.java`:
> ```java
> public static final int ORDER_TTL = 30000; // 30 giây
> ```

### Scenario 3: Hủy thủ công 🚫

1. **Tạo đơn hàng**
   - Tạo đơn hàng mới

2. **Hủy ngay**
   - Click nút "Hủy đơn"
   - Xác nhận trong dialog

3. **Kết quả**
   - Trạng thái chuyển sang CANCELLED ngay lập tức
   - Timer biến mất
   - Hiển thị lý do: "Hủy bởi người dùng"
   - Thống kê cập nhật

## 🎨 Giao diện features

### 🌈 Color Coding
- **Vàng** 🟡: Đơn hàng PENDING (đang chờ)
- **Xanh** 🟢: Đơn hàng PAID (đã thanh toán)
- **Đỏ** 🔴: Đơn hàng CANCELLED (đã hủy)

### 🔔 Toast Notifications
Hiển thị ở góc phải trên màn hình:
- ✅ **Success**: Màu xanh - Thao tác thành công
- ❌ **Error**: Màu đỏ - Có lỗi xảy ra
- ⚠️ **Warning**: Màu vàng - Cảnh báo
- ℹ️ **Info**: Màu xanh dương - Thông tin

### ⏱️ Real-time Updates
- Auto refresh mỗi 5 giây
- Timer đếm ngược mỗi giây
- Cập nhật thống kê tự động
- Không cần reload trang

### 📱 Responsive Design
- Desktop: Layout 2 cột
- Tablet: Layout 1 cột
- Mobile: Optimized cho màn hình nhỏ

## 🎬 Demo flow hoàn chỉnh

### Chuẩn bị
```powershell
# 1. Start RabbitMQ
docker-compose up -d

# 2. Start Spring Boot
./mvnw spring-boot:run

# 3. Mở 2 tab trình duyệt
# Tab 1: http://localhost:8080 (Demo UI)
# Tab 2: http://localhost:15672 (RabbitMQ Management)
```

### Thực hiện demo

1. **Giới thiệu**
   - Mở tab Demo UI
   - Giải thích các phần của giao diện
   - Chỉ TTL timer và statistics

2. **Demo tạo đơn**
   - Click "500K" để tạo nhanh
   - Chờ notification
   - Chỉ đơn hàng mới trong danh sách
   - Chỉ timer đếm ngược

3. **Demo thanh toán**
   - Tạo đơn hàng mới
   - Chờ vài giây
   - Click "Thanh toán"
   - Chỉ trạng thái đã thay đổi
   - Chỉ statistics đã update

4. **Demo hủy thủ công**
   - Tạo đơn hàng mới
   - Click "Hủy đơn"
   - Giải thích lý do hủy hiển thị

5. **Demo tự động hủy**
   - Tạo đơn hàng mới
   - Chuyển sang tab RabbitMQ
   - Vào Queues → `order.pending.queue`
   - Chỉ message trong queue
   - Chỉ TTL đang chạy
   - Quay lại tab Demo UI
   - Đợi timer về 00:00
   - Chỉ đơn hàng tự động chuyển sang CANCELLED

6. **Kết thúc**
   - Tạo nhiều đơn hàng cùng lúc
   - Một số thanh toán, một số hủy, một số để tự động hủy
   - Chỉ statistics tổng hợp

## 🛠️ Troubleshooting

### Trang không load
```
Lỗi: Cannot GET /
```
**Giải pháp**: 
- Kiểm tra Spring Boot đã chạy chưa
- Xem log console có lỗi không
- Thử truy cập: http://localhost:8080/index.html

### API không hoạt động
```
Lỗi: Failed to fetch
```
**Giải pháp**:
- Mở DevTools (F12) → Console tab
- Xem chi tiết lỗi
- Kiểm tra endpoint: http://localhost:8080/api/orders
- Restart Spring Boot

### Timer không đếm
**Giải pháp**:
- Refresh lại trang (F5)
- Xóa cache trình duyệt (Ctrl + Shift + R)
- Kiểm tra console có lỗi JavaScript không

### Đơn hàng không tự động hủy
**Giải pháp**:
1. Kiểm tra RabbitMQ đang chạy
2. Vào RabbitMQ Management → Queues
3. Xem có message trong `order.pending.queue` không
4. Check TTL configuration
5. Xem log Spring Boot console

## 🎯 Best Practices cho Demo

### ✅ DO
- Giải thích từng bước một cách rõ ràng
- Chỉ vào các phần quan trọng trên UI
- Mở RabbitMQ Management song song để so sánh
- Tạo nhiều đơn hàng để demo tốt hơn
- Giảm TTL xuống 30 giây để demo nhanh

### ❌ DON'T
- Đừng click quá nhanh
- Đừng tạo quá nhiều đơn hàng cùng lúc
- Đừng quên giải thích concept RabbitMQ
- Đừng bỏ qua phần timer đếm ngược
- Đừng quên show console log

## 📚 Thêm thông tin

### Kiểm tra console log
Mở DevTools (F12) để xem:
```javascript
🚀 App initialized
✅ Đã tạo đơn hàng: ORD-ABC123
📊 Orders refreshed: 3 orders
⏰ Timer started for ORD-ABC123
```

### Monitoring RabbitMQ
- URL: http://localhost:15672
- Username: guest
- Password: guest
- Queues to watch:
  - `order.pending.queue`
  - `order.paid.queue`
  - `order.cancelled.queue`

### Keyboard Shortcuts
- `F5`: Refresh trang
- `Ctrl + Shift + R`: Hard refresh (xóa cache)
- `F12`: Mở DevTools
- `Ctrl + Shift + C`: Inspect element

---

## 🎉 Ready to Demo!

Bây giờ bạn đã sẵn sàng để demo ứng dụng một cách chuyên nghiệp! 

**Good luck!** 🚀
