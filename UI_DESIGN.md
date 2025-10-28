# 🖼️ GIAO DIỆN WEB DEMO - PREVIEW

## 📱 Layout Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                          🐰 Hệ thống tự động hủy đơn hàng                       │
│                       TTL: 15 phút  │  ● Online                                 │
└─────────────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────┬──────────────────────────────────────────────────┐
│  📝 TẠO ĐƠN HÀNG MỚI       │  📋 DANH SÁCH ĐƠN HÀNG                          │
├────────────────────────────┤                                                  │
│                            │  ┌────────────────────────────────────────────┐  │
│  👤 Mã khách hàng          │  │ # ORD-A1B2C3D4        ⏳ ĐANG CHỜ        │  │
│  [user123_________]        │  │                                            │  │
│                            │  │ 👤 user001    💰 500,000 VNĐ              │  │
│  💰 Số tiền (VNĐ)          │  │ 🕐 28/10/2025 10:30:45                    │  │
│  [500000__________]        │  │                                            │  │
│                            │  │ ⏰ Thời gian còn lại: 14:35               │  │
│  [🛒 TẠO ĐƠN HÀNG]         │  │                                            │  │
│                            │  │ [💳 Thanh toán] [❌ Hủy đơn]              │  │
├────────────────────────────┤  └────────────────────────────────────────────┘  │
│  ⚡ TẠO NHANH              │                                                  │
│  [250K] [500K] [1M]        │  ┌────────────────────────────────────────────┐  │
│                            │  │ # ORD-XYZ789          ✅ ĐÃ THANH TOÁN   │  │
├────────────────────────────┤  │                                            │  │
│  📊 THỐNG KÊ               │  │ 👤 user002    💰 1,000,000 VNĐ            │  │
│                            │  │ 🕐 28/10/2025 09:15:20                    │  │
│  ⏳ Đang chờ:      2       │  └────────────────────────────────────────────┘  │
│  ✅ Đã thanh toán: 5       │                                                  │
│  ❌ Đã hủy:        3       │  ┌────────────────────────────────────────────┐  │
│                            │  │ # ORD-DEF456          ❌ ĐÃ HỦY          │  │
└────────────────────────────┤  │                                            │  │
                             │  │ 👤 user003    💰 750,000 VNĐ              │  │
┌────────────────────────────┤  │ 🕐 28/10/2025 08:00:00                    │  │
│  ℹ️ HƯỚNG DẪN DEMO         │  │ ❌ Hủy lúc: 28/10/2025 08:15:00           │  │
│                            │  │ Lý do: Hủy tự động do quá thời gian       │  │
│  1. Tạo đơn hàng           │  └────────────────────────────────────────────┘  │
│  2. Thanh toán hoặc Hủy    │                                                  │
│  3. Tự động hủy (15 phút)  │  [🔄 Làm mới] [🗑️ Xóa tất cả]                  │
│                            │                                                  │
│  🔗 RabbitMQ Console       │                                                  │
│  🔗 API Endpoint           │                                                  │
└────────────────────────────┴──────────────────────────────────────────────────┘
```

## 🎨 Màu sắc và Style

### Color Scheme
```
Background Gradient:  #667eea → #764ba2 (Purple gradient)
Card Background:      #ffffff (White)
Text Primary:         #111827 (Dark gray)
Text Secondary:       #6b7280 (Medium gray)
Border:              #e5e7eb (Light gray)

Status Colors:
  - PENDING:     #f59e0b (Orange/Yellow)
  - PAID:        #10b981 (Green)
  - CANCELLED:   #ef4444 (Red)
```

### Typography
```
Font Family:  'Segoe UI', Tahoma, Geneva, Verdana, sans-serif
Heading H1:   24px, Bold
Heading H2:   20px, Semi-bold
Body Text:    14px, Regular
Small Text:   12px, Regular
```

### Components

#### 🔘 Buttons
```
Primary Button:   Blue gradient background, white text
Success Button:   Green background, white text
Danger Button:    Red background, white text
Outline Button:   Transparent with white border
```

#### 📦 Cards
```
Border Radius:    12px
Padding:         20px
Shadow:          Soft shadow (0 4px 6px rgba(0,0,0,0.1))
Border Left:     4px colored based on status
```

#### ⏱️ Timer
```
Background:      Light yellow (#fef3c7)
Font:           Monospace (Courier New)
Size:           20px, Bold
Color:          Orange (normal), Red (< 2 minutes)
Animation:      Pulse when near expiration
```

## 📐 Responsive Breakpoints

### Desktop (> 1024px)
```
Layout:          2 columns (400px | flexible)
Statistics:      3 columns grid
```

### Tablet (768px - 1024px)
```
Layout:          1 column stacked
Statistics:      3 columns grid
```

### Mobile (< 768px)
```
Layout:          1 column stacked
Statistics:      1 column stacked
Header:          Simplified
```

## 🎭 Animations

### Slide In (Orders)
```css
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
Duration: 0.3s
```

### Toast Notification
```css
@keyframes toastSlideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
Duration: 0.3s
```

### Pulse (Status indicator)
```css
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
Duration: 2s (infinite)
```

### Spinner (Loading)
```css
@keyframes spin {
  to { transform: rotate(360deg); }
}
Duration: 1s (infinite)
```

## 🌟 Interactive States

### Hover Effects
```
Buttons:      translateY(-2px) + shadow increase
Cards:        translateY(-2px) + shadow increase
Links:        Color change + scale
```

### Focus States
```
Input Fields: Blue border + shadow glow
Buttons:      Outline visible
```

### Active States
```
Buttons:      Slightly darker background
Cards:        Border highlight
```

## 📱 Features Detail

### Real-time Updates
- ✅ Auto-refresh every 5 seconds
- ✅ Timer countdown every second
- ✅ Statistics update on every change
- ✅ Toast notifications for all actions

### User Interactions
- ✅ Click to create order
- ✅ Click to pay order
- ✅ Click to cancel order
- ✅ Click to refresh orders
- ✅ Click to clear all orders

### Visual Feedback
- ✅ Loading overlay during API calls
- ✅ Toast notifications for success/error
- ✅ Color-coded order status
- ✅ Countdown timer with warnings
- ✅ Empty state when no orders

## 🎬 Screenshots Walkthrough

### 1. Initial State (Empty)
```
┌──────────────────────────┐
│   No orders created yet   │
│   📥 Inbox icon           │
│   "Chưa có đơn hàng nào"  │
└──────────────────────────┘
```

### 2. After Creating Order
```
┌──────────────────────────┐
│ # ORD-ABC123  ⏳ PENDING │
│ Timer: 14:59              │
│ [Pay] [Cancel]            │
└──────────────────────────┘
```

### 3. After Payment
```
┌──────────────────────────┐
│ # ORD-ABC123  ✅ PAID    │
│ No timer                  │
│ No action buttons         │
└──────────────────────────┘
```

### 4. After Auto-Cancel
```
┌──────────────────────────┐
│ # ORD-ABC123  ❌ CANCELLED│
│ Cancelled at: ...         │
│ Reason: Auto timeout      │
└──────────────────────────┘
```

## 💡 UI/UX Best Practices Applied

✅ **Clear Visual Hierarchy**
- Important information is prominent
- Colors convey meaning
- Typography guides attention

✅ **Immediate Feedback**
- Loading states
- Success/error messages
- Visual changes on actions

✅ **Accessibility**
- High contrast colors
- Readable font sizes
- Keyboard navigation support

✅ **Responsive Design**
- Works on all screen sizes
- Touch-friendly on mobile
- Optimized layouts

✅ **Performance**
- Smooth animations
- Efficient re-renders
- Optimized images/icons

## 🔍 Browser Compatibility

✅ Chrome/Edge (Chromium)
✅ Firefox
✅ Safari
✅ Opera
✅ Mobile browsers

## 📦 Dependencies

### CSS
- Font Awesome 6.4.0 (Icons)
- Custom CSS (No framework)

### JavaScript
- Vanilla JavaScript (No framework)
- Fetch API for HTTP requests
- ES6+ features

### No build step required!
Just open in browser and it works! 🎉

---

**Design Philosophy**: Simple, clean, and functional. The UI should be intuitive enough that anyone can understand and use it without reading documentation.
