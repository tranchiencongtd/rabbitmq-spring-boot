# 📊 TRỰC QUAN HÓA HỆ THỐNG - TỰ ĐỘNG HỦY ĐƠN HÀNG

## 🎯 Bức tranh toàn cảnh

```
┌───────────────────────────────────────────────────────────────────────────┐
│                         HÀNH TRÌNH CỦA MỘT ĐƠN HÀNG                       │
└───────────────────────────────────────────────────────────────────────────┘

    👤 Khách hàng                🖥️  Server                   🐰 RabbitMQ
        │                           │                           │
        │  1. Tạo đơn hàng          │                           │
        │─────────────────────────►│                           │
        │                           │  2. Gửi vào queue         │
        │                           │─────────────────────────►│
        │                           │                           │
        │                           │                      ┌────▼────┐
        │                           │                      │ PENDING │
        │                           │                      │ ⏰ TTL  │
        │                           │                      └────┬────┘
        │                           │                           │
        │◄──────Nhận thông báo──────┤◄─────3. Consumer──────────┤
        │   "Vui lòng thanh toán"   │      xử lý                │
        │                           │                           │
        │                                                       │
        │                           │                           │
        ├─────────── CASE 1: THANH TOÁN ──────────────────────┤
        │                           │                           │
        │  4a. Thanh toán           │                           │
        │─────────────────────────►│                           │
        │                           │  5a. Update & send        │
        │                           │─────────────────────────►│
        │                           │                      ┌────▼────┐
        │                           │                      │  PAID   │
        │                           │                      │    ✅   │
        │                           │                      └────┬────┘
        │◄──────Xác nhận────────────┤◄─────6a. Consumer─────────┤
        │   "Thanh toán thành công" │      xử lý                │
        │                           │                           │
        │                                                       │
        │                           │                           │
        ├─────────── CASE 2: TỰ ĐỘNG HỦY ─────────────────────┤
        │                           │                           │
        │         ⏰ Đợi 15 phút...                             │
        │                           │                           │
        │                           │                      ┌────▼────┐
        │                           │                      │ PENDING │
        │                           │                      │ EXPIRED │
        │                           │                      └────┬────┘
        │                           │                           │
        │                           │                      [DLX route]
        │                           │                           │
        │                           │                      ┌────▼────┐
        │                           │                      │CANCELLED│
        │                           │                      │    ❌   │
        │                           │                      └────┬────┘
        │◄──────Thông báo───────────┤◄─────Consumer─────────────┤
        │   "Đơn hàng đã hủy"       │      xử lý                │
        │                           │                           │
```

## 🏗️ Kiến trúc các thành phần

```
┌─────────────────────────────────────────────────────────────────┐
│                        APPLICATION LAYER                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐      ┌──────────────┐      ┌──────────────┐ │
│  │   REST API   │      │   Service    │      │     DTO      │ │
│  │              │      │              │      │              │ │
│  │ - POST /     │      │ - Save order │      │ - Order      │ │
│  │   create     │◄────►│ - Get order  │◄────►│ - Request   │ │
│  │ - POST /pay  │      │ - Update     │      │              │ │
│  │ - POST /     │      │              │      │              │ │
│  │   cancel     │      │              │      │              │ │
│  │ - GET /      │      │              │      │              │ │
│  └──────┬───────┘      └──────────────┘      └──────────────┘ │
│         │                                                       │
└─────────┼───────────────────────────────────────────────────────┘
          │
          │ Gửi/Nhận message
          │
┌─────────▼───────────────────────────────────────────────────────┐
│                      MESSAGING LAYER                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐                        ┌──────────────┐      │
│  │   Producer   │                        │   Consumer   │      │
│  │              │                        │              │      │
│  │ - sendTo     │                        │ - handle     │      │
│  │   Pending    │                        │   Pending    │      │
│  │ - sendTo     │                        │ - handle     │      │
│  │   Paid       │                        │   Paid       │      │
│  │ - sendTo     │                        │ - handle     │      │
│  │   Cancelled  │                        │   Cancelled  │      │
│  │              │                        │              │      │
│  └──────┬───────┘                        └──────▲───────┘      │
│         │                                       │               │
│         │ RabbitTemplate         @RabbitListener│              │
│         │                                       │               │
└─────────┼───────────────────────────────────────┼───────────────┘
          │                                       │
          │                                       │
┌─────────▼───────────────────────────────────────┼───────────────┐
│                       RABBITMQ BROKER           │               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │                    order.exchange                          │ │
│  │                  (Direct Exchange)                         │ │
│  └───┬────────────────────┬────────────────────┬──────────────┘ │
│      │                    │                    │                 │
│      │ order.pending      │ order.paid         │ order.cancelled │
│      │                    │                    │                 │
│  ┌───▼──────────────┐ ┌───▼──────────────┐ ┌───▼──────────────┐│
│  │ order.pending    │ │ order.paid       │ │ order.cancelled  ││
│  │ .queue           │ │ .queue           │ │ .queue           ││
│  │                  │ │                  │ │                  ││
│  │ • TTL: 15min     │ │ ✅ Đã thanh toán │ │ ❌ Đã hủy        ││
│  │ • DLX: set       │ │                  │ │                  ││
│  └───┬──────────────┘ └──────────────────┘ └──────────────────┘│
│      │                                                           │
│      │ Message expire                                           │
│      │                                                           │
│  ┌───▼──────────────────────────────────────────────────────┐  │
│  │              order.dlx.exchange                           │  │
│  │            (Dead Letter Exchange)                         │  │
│  └───┬───────────────────────────────────────────────────────┘  │
│      │                                                           │
│      │ order.cancelled                                          │
│      │                                                           │
│      └──────────────────► Cancelled Queue                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Flow Chart Chi Tiết

```
                    START
                      │
                      ▼
        ┌─────────────────────────┐
        │   Client tạo đơn hàng   │
        │   POST /api/orders/     │
        │        create           │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  Tạo Order object       │
        │  orderId = random UUID  │
        │  status = PENDING       │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  Producer gửi message   │
        │  đến pending queue      │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  Message vào queue      │
        │  TTL bắt đầu đếm        │
        └────────────┬────────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │  Consumer xử lý         │
        │  - Lưu DB               │
        │  - Gửi email nhắc       │
        └────────────┬────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
    ┌─────────┐           ┌──────────┐
    │Thanh toán│           │Không làm │
    │trong TTL │           │   gì     │
    └────┬────┘           └─────┬────┘
         │                      │
         │                      │ TTL expire
         │                      │
         ▼                      ▼
    ┌─────────────┐     ┌──────────────┐
    │POST /pay    │     │Message expire│
    │             │     │→ DLX         │
    └─────┬───────┘     └──────┬───────┘
          │                    │
          ▼                    ▼
    ┌─────────────┐     ┌──────────────┐
    │Send to paid │     │Route to      │
    │queue        │     │cancelled     │
    └─────┬───────┘     │queue         │
          │             └──────┬───────┘
          │                    │
          ▼                    ▼
    ┌─────────────┐     ┌──────────────┐
    │Consumer:    │     │Consumer:     │
    │handlePaid() │     │handle        │
    │             │     │Cancelled()   │
    └─────┬───────┘     └──────┬───────┘
          │                    │
          ▼                    ▼
    ┌─────────────┐     ┌──────────────┐
    │Status = PAID│     │Status =      │
    │✅          │     │CANCELLED ❌  │
    └─────┬───────┘     └──────┬───────┘
          │                    │
          └──────────┬─────────┘
                     │
                     ▼
        ┌─────────────────────────┐
        │   Gửi email/SMS         │
        │   thông báo kết quả     │
        └────────────┬────────────┘
                     │
                     ▼
                    END
```

## 📈 Timeline Diagram

```
Minute 0                                    Minute 15
│                                                    │
├────────────────────────────────────────────────────┤
│                                                    │
│  Tạo đơn                                    Expire │
│     │                                           │  │
│     ▼                                           │  │
│  ⏰ TTL bắt đầu                                 │  │
│     ├───────────────────────────────────────────┤  │
│     │         Chờ thanh toán (15 phút)        │  │
│     └───────────────────────────────────────────┘  │
│                                                    │
│                                                    │
│  ┌──────────────────┐                             │
│  │   Thanh toán?    │                             │
│  └────┬────────┬────┘                             │
│       │        │                                   │
│       │ CÓ     │ KHÔNG                            │
│       │        │                                   │
│       ▼        └──────────────────────────────────►│
│   ✅ PAID                                    Expire│
│   (Kết thúc)                                      │
│                                                    ▼
│                                              ❌ CANCELLED
│                                              (Tự động hủy)
│
└────────────────────────────────────────────────────┘

Chú thích:
⏰ TTL Timer đang chạy
✅ Thanh toán thành công → Không bị hủy
❌ Hết TTL → Tự động hủy qua DLX
```

## 🎭 State Diagram - Trạng thái đơn hàng

```
                    ┌─────────┐
                    │  START  │
                    └────┬────┘
                         │ create
                         ▼
                   ┌───────────┐
          ┌────────│  PENDING  │────────┐
          │        └───────────┘        │
          │              │              │
          │ cancel       │ TTL expire   │ pay
          │              │              │
          ▼              ▼              ▼
    ┌───────────┐  ┌───────────┐  ┌────────┐
    │ CANCELLED │  │ CANCELLED │  │  PAID  │
    │ (Manual)  │  │  (Auto)   │  │        │
    └───────────┘  └───────────┘  └────────┘
          │              │              │
          │              │              │
          └──────────────┴──────────────┘
                         │
                         ▼
                    ┌─────────┐
                    │   END   │
                    └─────────┘
```

## 🎪 Các Actor và Vai trò

```
┌────────────────────────────────────────────────────────┐
│                    SYSTEM ACTORS                        │
├────────────────────────────────────────────────────────┤
│                                                         │
│  👤 Khách hàng (Client)                                │
│     ├─ Tạo đơn hàng                                    │
│     ├─ Thanh toán đơn hàng                             │
│     └─ Hủy đơn hàng                                    │
│                                                         │
│  🌐 REST API (OrderController)                         │
│     ├─ Nhận request từ client                          │
│     ├─ Validate dữ liệu                                │
│     ├─ Gọi Producer/Service                            │
│     └─ Trả về response                                 │
│                                                         │
│  📤 Producer (OrderProducer)                           │
│     ├─ Gửi message vào RabbitMQ                        │
│     ├─ Serialize object thành JSON                     │
│     └─ Set routing key                                 │
│                                                         │
│  🐰 RabbitMQ Broker                                    │
│     ├─ Nhận và lưu trữ message                         │
│     ├─ Quản lý TTL                                     │
│     ├─ Route message qua DLX khi expire                │
│     └─ Deliver message cho consumer                    │
│                                                         │
│  📥 Consumer (OrderConsumer)                           │
│     ├─ Lắng nghe queue                                 │
│     ├─ Nhận và xử lý message                           │
│     ├─ Cập nhật database                               │
│     └─ Gửi notification                                │
│                                                         │
│  💾 Service (OrderService)                             │
│     ├─ Quản lý business logic                          │
│     ├─ CRUD operations                                 │
│     └─ Lưu trữ dữ liệu (in-memory/DB)                  │
│                                                         │
└────────────────────────────────────────────────────────┘
```

## 🎯 Các Scenario Thực Tế

### 📱 Scenario E-commerce

```
Thứ 2, 9:00 AM
│  Khách hàng đặt 1 áo sơ mi trên website
│  Giá: 500,000 VNĐ
│
├─► Hệ thống:
│   • Tạo đơn hàng ORD-ABC123
│   • Giữ hàng trong kho
│   • Gửi email: "Vui lòng thanh toán trong 15 phút"
│   • Message vào pending queue với TTL = 15 phút
│
├─► 9:05 AM - Khách hàng thanh toán qua VNPay
│   • API nhận webhook từ VNPay
│   • Gọi POST /api/orders/ORD-ABC123/pay
│   • Đơn hàng chuyển sang PAID
│   • Gửi email xác nhận
│   • Bắt đầu đóng gói và giao hàng
│
└─► Kết quả: ✅ Thành công, hàng được giao
```

### ⏰ Scenario Timeout

```
Thứ 3, 2:00 PM
│  Khách hàng đặt 1 điện thoại
│  Giá: 15,000,000 VNĐ
│
├─► Hệ thống:
│   • Tạo đơn hàng ORD-XYZ789
│   • Giữ hàng trong kho
│   • Gửi email nhắc nhở
│   • TTL = 15 phút
│
├─► 2:01 - 2:14 PM
│   • Khách hàng do dự, chưa thanh toán
│   • Gửi SMS nhắc: "Còn 5 phút"
│
├─► 2:15 PM - TTL hết hạn
│   • Message tự động expire
│   • Chuyển qua DLX → cancelled queue
│   • Consumer xử lý:
│     - Cập nhật status = CANCELLED
│     - Hoàn trả điện thoại về kho
│     - Gửi email: "Đơn hàng đã hủy do quá thời gian"
│
└─► Kết quả: ❌ Đơn hàng bị hủy tự động
```

## 💡 Best Practices

```
✅ NÊN:
  • Set TTL phù hợp với loại sản phẩm
  • Gửi nhiều lần nhắc nhở (5 phút, 2 phút, 30 giây)
  • Log đầy đủ để debug
  • Có retry mechanism cho consumer
  • Monitor queue depth
  • Backup database thường xuyên

❌ KHÔNG NÊN:
  • Set TTL quá ngắn (< 5 phút)
  • Không validate input
  • Bỏ qua error handling
  • Hard-code config
  • Không test trước khi deploy
  • Quên enable durable queues
```

---

**💭 Ghi chú:** Tất cả diagram trên đây được thiết kế để dễ hiểu và trực quan hóa toàn bộ luồng xử lý của hệ thống tự động hủy đơn hàng sử dụng RabbitMQ.
