# 🔄 Luồng xử lý tự động hủy đơn hàng

## 📊 Sơ đồ tổng quan

```
┌─────────────┐
│   Client    │
│  (REST API) │
└──────┬──────┘
       │
       │ POST /api/orders/create
       ▼
┌─────────────────────┐
│  OrderController    │
│  - Tạo Order        │
│  - Gọi Producer     │
└──────┬──────────────┘
       │
       │ sendOrderToPending()
       ▼
┌─────────────────────┐
│  OrderProducer      │
│  - Gửi message      │
└──────┬──────────────┘
       │
       │ convertAndSend()
       ▼
┌──────────────────────────────┐
│     order.exchange           │
│   (Direct Exchange)          │
└──────┬───────────────────────┘
       │
       │ routing-key: "order.pending"
       ▼
┌────────────────────────────────────┐
│   order.pending.queue              │
│   • TTL = 15 phút                  │
│   • DLX = order.dlx.exchange       │
│   • DLX routing = order.cancelled  │
└──────┬─────────────────────────────┘
       │
       │ Listener
       ▼
┌─────────────────────┐
│  OrderConsumer      │
│  - Nhận order       │
│  - Lưu vào DB       │
│  - Gửi email nhắc   │
└─────────────────────┘
```

## ⏱️ Luồng 1: Đơn hàng tự động hủy (Timeout)

```
Thời gian: 0 phút
┌────────────────┐
│ Tạo đơn hàng   │
│ Status: PENDING│
└────────┬───────┘
         │
         │ Message vào queue pending
         │ TTL bắt đầu đếm ngược
         ▼
Thời gian: 1-14 phút
┌────────────────┐
│  Chờ thanh toán│
│  ⏰ Đếm ngược  │
└────────┬───────┘
         │
         │ KHÔNG có thanh toán
         ▼
Thời gian: 15 phút (TTL hết hạn)
┌──────────────────────────┐
│  Message EXPIRE          │
│  Chuyển sang DLX         │
└──────┬───────────────────┘
       │
       │ Dead Letter Exchange
       │ routing-key: "order.cancelled"
       ▼
┌──────────────────────────┐
│  order.dlx.exchange      │
│  (Dead Letter Exchange)  │
└──────┬───────────────────┘
       │
       │ Route to cancelled queue
       ▼
┌──────────────────────────┐
│  order.cancelled.queue   │
└──────┬───────────────────┘
       │
       │ Listener
       ▼
┌──────────────────────────┐
│  OrderConsumer           │
│  - Cập nhật status       │
│  - Status = CANCELLED    │
│  - Hoàn trả hàng về kho  │
│  - Gửi email thông báo   │
└──────────────────────────┘
```

## ✅ Luồng 2: Thanh toán thành công

```
Thời gian: 0 phút
┌────────────────┐
│ Tạo đơn hàng   │
│ Status: PENDING│
└────────┬───────┘
         │
         │ Message vào queue pending
         ▼
Thời gian: 5 phút (ví dụ)
┌────────────────────┐
│  Client gọi API    │
│  POST .../pay      │
└────────┬───────────┘
         │
         │ OrderController
         ▼
┌────────────────────────┐
│  OrderProducer         │
│  sendOrderToPaid()     │
└────────┬───────────────┘
         │
         │ routing-key: "order.paid"
         ▼
┌────────────────────────┐
│  order.exchange        │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  order.paid.queue      │
└────────┬───────────────┘
         │
         │ Listener
         ▼
┌────────────────────────┐
│  OrderConsumer         │
│  - Cập nhật PAID       │
│  - Xử lý đóng gói      │
│  - Gửi email xác nhận  │
└────────────────────────┘

NOTE: Message trong pending queue vẫn còn,
      nhưng sẽ bị BỎ QUA khi expire vì
      status đã là PAID
```

## ❌ Luồng 3: Hủy thủ công

```
Thời gian: 0 phút
┌────────────────┐
│ Tạo đơn hàng   │
│ Status: PENDING│
└────────┬───────┘
         │
         │ Message vào queue pending
         ▼
Thời gian: 3 phút (ví dụ)
┌────────────────────┐
│  Client gọi API    │
│  POST .../cancel   │
└────────┬───────────┘
         │
         │ OrderController
         ▼
┌────────────────────────┐
│  OrderProducer         │
│  sendOrderToCancelled()│
└────────┬───────────────┘
         │
         │ routing-key: "order.cancelled"
         │ (gửi trực tiếp, không qua DLX)
         ▼
┌────────────────────────┐
│  order.exchange        │
└────────┬───────────────┘
         │
         ▼
┌────────────────────────┐
│  order.cancelled.queue │
└────────┬───────────────┘
         │
         │ Listener
         ▼
┌────────────────────────┐
│  OrderConsumer         │
│  - Cập nhật CANCELLED  │
│  - Lý do: User cancel  │
│  - Hoàn trả hàng       │
│  - Gửi email thông báo │
└────────────────────────┘
```

## 🏗️ Kiến trúc chi tiết

### Queue Configuration

```
order.pending.queue:
├── Durable: true
├── Auto-delete: false
├── Arguments:
│   ├── x-message-ttl: 900000 (15 phút)
│   ├── x-dead-letter-exchange: "order.dlx.exchange"
│   └── x-dead-letter-routing-key: "order.cancelled"
└── Binding:
    ├── Exchange: order.exchange
    └── Routing Key: order.pending

order.cancelled.queue:
├── Durable: true
├── Auto-delete: false
└── Binding:
    ├── Exchange: order.dlx.exchange
    └── Routing Key: order.cancelled

order.paid.queue:
├── Durable: true
├── Auto-delete: false
└── Binding:
    ├── Exchange: order.exchange
    └── Routing Key: order.paid
```

### Message Flow Timeline

```
T=0s    : Tạo đơn hàng → Message gửi vào pending queue
T=1s    : Consumer nhận message, lưu vào DB, gửi email nhắc
T=0-900s: Message chờ trong queue (TTL đếm ngược)

--- Trường hợp 1: Thanh toán ---
T=300s  : Client gọi API pay
T=301s  : Message gửi vào paid queue
T=302s  : Consumer xử lý thanh toán, status = PAID
T=900s  : Message expire trong pending queue
T=901s  : Consumer nhận message expire, kiểm tra status
T=902s  : Status = PAID → BỎ QUA, không hủy đơn

--- Trường hợp 2: Không thanh toán ---
T=900s  : Message expire, chuyển sang DLX
T=901s  : DLX route message đến cancelled queue
T=902s  : Consumer xử lý hủy đơn, status = CANCELLED
```

## 🎯 Key Components

### 1. Dead Letter Exchange (DLX)
- Exchange nhận message khi:
  - Message expire (TTL hết hạn)
  - Message bị reject
  - Queue đầy (max length)

### 2. Message TTL (Time To Live)
- Thời gian sống của message trong queue
- Sau khi hết TTL → message chuyển sang DLX
- Có thể set per-queue hoặc per-message

### 3. Routing Key
- `order.pending`: Route đến pending queue
- `order.paid`: Route đến paid queue  
- `order.cancelled`: Route đến cancelled queue (từ DLX)

## 📈 Ưu điểm của kiến trúc này

1. **Tự động hóa hoàn toàn**: Không cần cronjob
2. **Chính xác về thời gian**: RabbitMQ đảm bảo TTL chính xác
3. **Không mất message**: Message được lưu trữ trong queue
4. **Scalable**: Có thể thêm nhiều consumer
5. **Decoupled**: Các component độc lập với nhau
6. **Fault tolerant**: Message được persist vào disk

## 🔍 Monitoring Points

### 1. Queue Metrics
- `order.pending.queue`:
  - Messages: Số đơn đang chờ
  - TTL remaining: Thời gian còn lại
  
- `order.paid.queue`:
  - Messages: Số đơn đã thanh toán
  
- `order.cancelled.queue`:
  - Messages: Số đơn bị hủy

### 2. Exchange Metrics
- Message rate: Số message/giây
- Routing success: Tỷ lệ route thành công

### 3. Consumer Metrics
- Processing time: Thời gian xử lý
- Error rate: Tỷ lệ lỗi
- Throughput: Số message xử lý/giây

## 🛠️ Troubleshooting Guide

### Message không tự động hủy?
1. Kiểm tra TTL trong OrderCancellationConfig
2. Xem message có vào pending queue không
3. Kiểm tra DLX đã được binding đúng chưa

### Consumer không nhận message?
1. Kiểm tra @RabbitListener annotation
2. Xem log có error không
3. Kiểm tra connection tới RabbitMQ

### Đơn hàng đã thanh toán vẫn bị hủy?
1. Kiểm tra logic trong handleCancelledOrder
2. Đảm bảo status được update đúng
3. Xem log để trace flow
