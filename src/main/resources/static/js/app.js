// API URL
const API_URL = '/api/orders';

// State
let orders = new Map();
let timers = new Map();
let refreshInterval = null;

// Init
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 App started');
    
    document.getElementById('createOrderForm').addEventListener('submit', (e) => {
        e.preventDefault();
        createOrder();
    });
    
    loadOrders();
    startAutoRefresh();
});

// Load orders
async function loadOrders() {
    try {
        const res = await fetch(API_URL);
        const data = await res.json();
        
        if (data.success && data.orders) {
            updateUI(data.orders);
        }
    } catch (err) {
        console.error('Load error:', err);
    }
}

// Auto refresh với tần suất thông minh
function startAutoRefresh() {
    if (refreshInterval) {
        clearInterval(refreshInterval);
    }
    
    // Check mỗi 5 giây nếu có pending orders, ngược lại 15 giây
    const checkAndRefresh = () => {
        const hasPending = Array.from(orders.values()).some(o => o.status === 'PENDING');
        const interval = hasPending ? 5000 : 15000; // 5s hoặc 15s
        
        if (refreshInterval) {
            clearInterval(refreshInterval);
        }
        refreshInterval = setInterval(loadOrders, interval);
        console.log(`🔄 Auto-refresh: ${interval/1000}s (hasPending: ${hasPending})`);
    };
    
    // Initial setup
    checkAndRefresh();
    
    // Re-check interval sau mỗi lần load
    setInterval(checkAndRefresh, 30000); // Check mỗi 30s
}

// Update UI
function updateUI(newOrders) {
    const container = document.getElementById('orders-container');
    
    // Convert to Map
    const newMap = new Map(newOrders.map(o => [o.orderId, o]));
    
    // Empty state
    if (newMap.size === 0) {
        container.innerHTML = '<div class="empty">Chưa có đơn hàng</div>';
        orders.clear();
        timers.forEach(t => clearInterval(t));
        timers.clear();
        updateStats();
        return;
    }
    
    // Remove empty
    const empty = container.querySelector('.empty');
    if (empty) empty.remove();
    
    // Get existing cards
    const existing = new Map();
    container.querySelectorAll('.order-card').forEach(card => {
        const id = card.dataset.id;
        if (id) existing.set(id, { card, status: card.dataset.status });
    });
    
    // Sort by date
    const sorted = Array.from(newMap.values()).sort((a, b) => 
        new Date(b.createdAt) - new Date(a.createdAt)
    );
    
    // Update cards
    sorted.forEach(order => {
        const ex = existing.get(order.orderId);
        
        if (ex && ex.status === order.status) {
            // Same status - skip
            return;
        }
        
        if (ex) {
            // Status changed - replace
            const newCard = createCard(order);
            ex.card.replaceWith(newCard);
            
            if (order.status !== 'PENDING') {
                stopTimer(order.orderId);
            }
        } else {
            // New card
            container.appendChild(createCard(order));
            
            if (order.status === 'PENDING') {
                startTimer(order.orderId, order.createdAt);
            }
        }
    });
    
    // Remove old cards
    existing.forEach((ex, id) => {
        if (!newMap.has(id)) {
            ex.card.remove();
            stopTimer(id);
        }
    });
    
    orders = newMap;
    updateStats();
}

// Create card
function createCard(order) {
    const card = document.createElement('div');
    card.className = `order-card ${order.status.toLowerCase()}`;
    card.dataset.id = order.orderId;
    card.dataset.status = order.status;
    
    const statusInfo = {
        PENDING: { icon: '⏳', text: 'Chờ thanh toán', class: 'pending' },
        PAID: { icon: '✅', text: 'Đã thanh toán', class: 'paid' },
        CANCELLED: { icon: '❌', text: 'Đã hủy', class: 'cancelled' }
    };
    
    const info = statusInfo[order.status] || statusInfo.PENDING;
    
    card.innerHTML = `
        <div class="card-header">
            <div class="order-id">${order.orderId}</div>
            <div class="status ${info.class}">${info.icon} ${info.text}</div>
        </div>
        
        <div class="card-body">
            <div class="info-row">
                <span>👤 Khách hàng:</span>
                <strong>${order.userId}</strong>
            </div>
            <div class="info-row">
                <span>💰 Số tiền:</span>
                <strong>${formatMoney(order.amount)}</strong>
            </div>
            <div class="info-row">
                <span>🕐 Tạo lúc:</span>
                <span>${formatTime(order.createdAt)}</span>
            </div>
            ${order.cancelledAt ? `
            <div class="info-row">
                <span>🚫 Hủy lúc:</span>
                <span>${formatTime(order.cancelledAt)}</span>
            </div>
            ` : ''}
            ${order.cancelReason ? `
            <div class="info-row">
                <span>📝 Lý do:</span>
                <span>${order.cancelReason}</span>
            </div>
            ` : ''}
        </div>
        
        ${order.status === 'PENDING' ? `
            <div class="timer" id="timer-${order.orderId}">
                <span>⏱️ Còn lại:</span>
                <strong id="timer-value-${order.orderId}">--:--</strong>
            </div>
            
            <div class="card-actions">
                <button onclick="pay('${order.orderId}')" class="btn btn-success">
                    💳 Thanh toán
                </button>
                <button onclick="cancel('${order.orderId}')" class="btn btn-danger">
                    ❌ Hủy đơn
                </button>
            </div>
        ` : ''}
    `;
    
    return card;
}

// Timer
function startTimer(id, createdAt) {
    if (timers.has(id)) return;
    
    const TTL = 15 * 60 * 1000;
    const created = new Date(createdAt).getTime();
    
    const update = () => {
        const now = Date.now();
        const remaining = TTL - (now - created);
        
        const value = document.getElementById(`timer-value-${id}`);
        if (!value) {
            stopTimer(id);
            return;
        }
        
        if (remaining <= 0) {
            value.textContent = 'HẾT HẠN';
            value.style.color = '#ef4444';
            stopTimer(id);
            
            // Refresh multiple times để đảm bảo lấy được status mới
            setTimeout(() => {
                loadOrders(); // Lần 1: Sau 2 giây
                setTimeout(loadOrders, 2000); // Lần 2: Sau 4 giây
                setTimeout(loadOrders, 5000); // Lần 3: Sau 7 giây
            }, 2000);
            return;
        }
        
        const mins = Math.floor(remaining / 60000);
        const secs = Math.floor((remaining % 60000) / 1000);
        value.textContent = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
        
        if (remaining < 2 * 60 * 1000) {
            value.style.color = '#ef4444';
        }
    };
    
    update();
    timers.set(id, setInterval(update, 1000));
}

function stopTimer(id) {
    if (timers.has(id)) {
        clearInterval(timers.get(id));
        timers.delete(id);
    }
}

// Actions
async function createOrder() {
    const userId = document.getElementById('userId').value.trim();
    const amount = parseFloat(document.getElementById('amount').value);
    
    if (!userId || !amount || amount < 1000) {
        alert('Vui lòng nhập đầy đủ thông tin!');
        return;
    }
    
    try {
        const res = await fetch(`${API_URL}/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId, amount })
        });
        
        const data = await res.json();
        
        if (data.success) {
            toast('✅ Đã tạo đơn hàng ' + data.orderId);
            document.getElementById('createOrderForm').reset();
            loadOrders();
        } else {
            alert('Lỗi: ' + data.message);
        }
    } catch (err) {
        alert('Lỗi kết nối: ' + err.message);
    }
}

async function pay(id) {
    if (!confirm('Xác nhận thanh toán?')) return;
    
    try {
        const res = await fetch(`${API_URL}/${id}/pay`, { method: 'POST' });
        const data = await res.json();
        
        if (data.success) {
            toast('✅ Đã thanh toán đơn ' + id);
            stopTimer(id);
            loadOrders();
        } else {
            alert('Lỗi: ' + data.message);
        }
    } catch (err) {
        alert('Lỗi: ' + err.message);
    }
}

async function cancel(id) {
    if (!confirm('Xác nhận hủy đơn?')) return;
    
    try {
        const res = await fetch(`${API_URL}/${id}/cancel`, { method: 'POST' });
        const data = await res.json();
        
        if (data.success) {
            toast('❌ Đã hủy đơn ' + id);
            stopTimer(id);
            loadOrders();
        } else {
            alert('Lỗi: ' + data.message);
        }
    } catch (err) {
        alert('Lỗi: ' + err.message);
    }
}

// Stats
function updateStats() {
    const arr = Array.from(orders.values());
    document.getElementById('stat-pending').textContent = arr.filter(o => o.status === 'PENDING').length;
    document.getElementById('stat-paid').textContent = arr.filter(o => o.status === 'PAID').length;
    document.getElementById('stat-cancelled').textContent = arr.filter(o => o.status === 'CANCELLED').length;
}

// Utils
function formatMoney(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
}

function formatTime(date) {
    return new Date(date).toLocaleString('vi-VN');
}

function toast(msg) {
    const div = document.createElement('div');
    div.className = 'toast';
    div.textContent = msg;
    document.body.appendChild(div);
    setTimeout(() => div.classList.add('show'), 10);
    setTimeout(() => {
        div.classList.remove('show');
        setTimeout(() => div.remove(), 300);
    }, 3000);
}
