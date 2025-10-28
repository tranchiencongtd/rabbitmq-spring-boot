# 🎬 DEMO SCRIPT - 5 PHÚT

## Chuẩn bị (1 phút)

```powershell
# Terminal 1: Start RabbitMQ
docker-compose up -d

# Terminal 2: Start Spring Boot
./mvnw spring-boot:run

# Trình duyệt:
# Tab 1: http://localhost:8080 (Demo UI)
# Tab 2: http://localhost:15672 (RabbitMQ - guest/guest)
```

## Script Demo (4 phút)

### PHẦN 1: Giới thiệu (30 giây)

> 👋 "Xin chào! Hôm nay tôi sẽ demo hệ thống tự động hủy đơn hàng sử dụng RabbitMQ."
>
> 📱 [Chỉ vào màn hình]
> "Đây là giao diện quản lý đơn hàng. Bên trái là form tạo đơn, bên phải là danh sách đơn hàng real-time."

### PHẦN 2: Tạo đơn hàng (30 giây)

> 🛒 [Click nút "500K"]
>
> "Tôi sẽ tạo một đơn hàng 500,000 VNĐ. Đơn hàng vừa được tạo và gửi vào RabbitMQ."
>
> ⏰ [Chỉ vào timer đếm ngược]
>
> "Đơn hàng này có 15 phút để thanh toán. Timer bắt đầu đếm ngược từ 15:00."

### PHẦN 3: RabbitMQ (30 giây)

> 🐰 [Chuyển sang tab RabbitMQ]
>
> "Trong RabbitMQ Management Console, tôi có 3 queues:"
> - `order.pending.queue` - Đơn đang chờ thanh toán
> - `order.paid.queue` - Đơn đã thanh toán
> - `order.cancelled.queue` - Đơn đã hủy
>
> [Click vào order.pending.queue]
>
> "Message của đơn hàng đang nằm ở đây với TTL 15 phút."

### PHẦN 4: Demo thanh toán (30 giây)

> 💳 [Quay lại tab Demo UI, tạo đơn mới]
>
> "Bây giờ tôi tạo đơn mới và thanh toán ngay."
>
> [Tạo đơn → Click "Thanh toán"]
>
> ✅ "Đơn hàng chuyển sang trạng thái ĐÃ THANH TOÁN. Timer biến mất vì đơn này sẽ không bị hủy nữa."

### PHẦN 5: Demo hủy thủ công (30 giây)

> ❌ [Tạo đơn mới]
>
> "Khách hàng cũng có thể hủy đơn bất cứ lúc nào."
>
> [Click "Hủy đơn"]
>
> "Đơn hàng chuyển sang ĐÃ HỦY với lý do 'Hủy bởi người dùng'."

### PHẦN 6: Demo tự động hủy (1 phút)

> ⏱️ "Phần quan trọng nhất: TỰ ĐỘNG HỦY"
>
> [Tạo đơn mới]
>
> "Đơn hàng này tôi sẽ không làm gì cả. Chúng ta sẽ quan sát timer."
>
> ⚠️ [Chỉ vào timer khi còn < 2 phút]
>
> "Khi còn dưới 2 phút, timer chuyển sang màu đỏ cảnh báo."
>
> 💥 [Khi timer về 00:00]
>
> "Timer hết! Message trong RabbitMQ sẽ expire và được chuyển qua Dead Letter Exchange."
>
> [Đợi vài giây, trang tự động refresh]
>
> ❌ "Đơn hàng tự động chuyển sang ĐÃ HỦY với lý do 'Hủy tự động do quá thời gian thanh toán'."

### PHẦN 7: Kết luận (30 giây)

> 📊 [Chỉ vào statistics]
>
> "Statistics được cập nhật real-time. Hiện tại:"
> - X đơn đang chờ
> - Y đơn đã thanh toán
> - Z đơn đã hủy
>
> 🎯 "Hệ thống này giúp:"
> 1. Tự động giải phóng hàng tồn kho
> 2. Không cần cronjob hay scheduled task
> 3. Đáng tin cậy với RabbitMQ
> 4. Dễ dàng scale với nhiều consumer
>
> 🙏 "Cảm ơn các bạn đã theo dõi!"

---

## 🎓 Tips cho Demo tốt

### ✅ TRƯỚC KHI DEMO

- [ ] Test chạy thử 1 lần đầy đủ
- [ ] Đóng các ứng dụng không cần thiết
- [ ] Zoom browser lên 110-125% cho dễ nhìn
- [ ] Chuẩn bị 2 màn hình nếu có thể
- [ ] Mở sẵn tất cả các tab cần thiết
- [ ] Giảm TTL xuống 30 giây để demo nhanh

### ✅ TRONG KHI DEMO

- [ ] Nói chậm và rõ ràng
- [ ] Chỉ vào những gì đang nói
- [ ] Tạm dừng để người xem hiểu
- [ ] Giải thích concept, không chỉ click
- [ ] Nhấn mạnh phần quan trọng
- [ ] Sẵn sàng trả lời câu hỏi

### ✅ SAU KHI DEMO

- [ ] Mở console log cho người tò mò
- [ ] Chia sẻ link GitHub repository
- [ ] Giải đáp thắc mắc
- [ ] Nhận feedback

---

## 🚀 Demo nhanh (2 phút version)

Nếu chỉ có 2 phút:

1. **Tạo đơn** (15s)
2. **Chỉ timer** (15s)
3. **Thanh toán 1 đơn** (20s)
4. **Hủy 1 đơn** (20s)
5. **Giải thích tự động hủy** (30s)
6. **Kết luận** (20s)

---

## 💬 Câu hỏi thường gặp

**Q: TTL có thể điều chỉnh được không?**
> A: Có! Trong `OrderCancellationConfig.java`, thay đổi `ORDER_TTL` từ 900000 (15 phút) thành giá trị bạn muốn.

**Q: Có thể scale không?**
> A: Có! Chạy nhiều instance của consumer, RabbitMQ sẽ tự động phân phối message.

**Q: Đơn đã thanh toán có bị hủy không?**
> A: KHÔNG! Consumer kiểm tra status trước khi hủy. Nếu đã PAID thì bỏ qua.

**Q: Message có mất không?**
> A: KHÔNG! Queue được set durable, message được persist vào disk.

**Q: Performance như thế nào?**
> A: Rất tốt! RabbitMQ có thể xử lý hàng nghìn message/giây.

---

## 📝 Checklist cuối cùng

```
[ ] RabbitMQ running?
[ ] Spring Boot running?
[ ] Browser tabs opened?
[ ] TTL reduced to 30s?
[ ] Audio/mic working?
[ ] Screen sharing ready?
[ ] Backup plan prepared?
[ ] Deep breath taken? 😊
```

---

## 🎉 Sẵn sàng!

**Break a leg!** 🍀

Remember: 
- Tự tin
- Nhiệt tình
- Giải thích rõ ràng
- Có gì sai thì cười và tiếp tục 😄
