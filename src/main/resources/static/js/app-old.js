// API Base URL
const API_BASE_URL = '/api/orders';

// Global state
let orders = {};
let timers = {};
let autoRefreshInterval = null;

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 App initialized');
    
    // Setup event listeners
    setupEventListeners();
    
    // Load initial data
    refreshOrders();
    
    // Auto refresh every 10 seconds (giảm tần suất để mượt hơn)
    autoRefreshInterval = setInterval(refreshOrders, 10000);
    
    // Get TTL info
    getTTLInfo();
});

// Setup event listeners
function setupEventListeners() {
    // Create order form
    const form = document.getElementById('createOrderForm');
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        createOrder();
    });
}

// Get TTL info from server
async function getTTLInfo() {
    // Default to 15 minutes
    document.getElementById('ttl-info').textContent = '15 phút';
}

// Create order
async function createOrder() {
    const userId = document.getElementById('userId').value.trim();
    const amount = parseFloat(document.getElementById('amount').value);
    
    if (!userId || !amount || amount < 1000) {
        showToast('error', 'Lỗi', 'Vui lòng nhập đầy đủ thông tin hợp lệ');
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userId, amount })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('success', 'Thành công', `Đã tạo đơn hàng ${data.orderId}`);
            document.getElementById('createOrderForm').reset();
            await refreshOrders();
        } else {
            showToast('error', 'Lỗi', data.message || 'Không thể tạo đơn hàng');
        }
    } catch (error) {
        console.error('Error creating order:', error);
        showToast('error', 'Lỗi', 'Không thể kết nối đến server');
    } finally {
        showLoading(false);
    }
}

// Create quick order
async function createQuickOrder(userId, amount) {
    document.getElementById('userId').value = userId;
    document.getElementById('amount').value = amount;
    await createOrder();
}

// Refresh orders
async function refreshOrders() {
    try {
        const response = await fetch(`${API_BASE_URL}`);
        const data = await response.json();
        
        if (data.success) {
            orders = {};
            if (data.orders && Array.isArray(data.orders)) {
                data.orders.forEach(order => {
                    orders[order.orderId] = order;
                });
            }
            renderOrders();
            updateStatistics();
        }
    } catch (error) {
        console.error('Error fetching orders:', error);
    }
}

// Render orders (với DOM diffing - không bị giật)
function renderOrders() {
    const container = document.getElementById('orders-container');
    const orderList = Object.values(orders);
    
    // Remove empty state if exists
    const emptyState = container.querySelector('.empty-state');
    if (emptyState && orderList.length > 0) {
        emptyState.remove();
    }
    
    if (orderList.length === 0) {
        // Only add empty state if not already there
        if (!emptyState) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-inbox"></i>
                    <p>Chưa có đơn hàng nào</p>
                    <small>Tạo đơn hàng mới để bắt đầu</small>
                </div>
            `;
        }
        return;
    }
    
    // Sort by created date (newest first)
    orderList.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    
    // Build map of existing cards by orderId
    const existingCardsMap = new Map();
    container.querySelectorAll('.order-card').forEach(card => {
        const orderId = card.dataset.orderId;
        if (orderId) {
            existingCardsMap.set(orderId, {
                element: card,
                status: card.dataset.status
            });
        }
    });
    
    // Track which orders we've processed
    const processedOrderIds = new Set();
    
    // Update or add cards
    orderList.forEach((order, index) => {
        processedOrderIds.add(order.orderId);
        const existing = existingCardsMap.get(order.orderId);
        
        if (existing) {
            // Card exists - check if status changed
            if (existing.status !== order.status) {
                // Status changed - update the card
                const newCard = createOrderElement(order);
                existing.element.replaceWith(newCard);
                
                // Handle timers
                if (order.status === 'PENDING') {
                    startTimer(order.orderId, order.createdAt);
                } else {
                    // Stop timer if status changed from PENDING
                    if (timers[order.orderId]) {
                        clearInterval(timers[order.orderId]);
                        delete timers[order.orderId];
                    }
                }
            }
            // Status unchanged - don't touch (keeps timer running smoothly)
        } else {
            // New card - create and insert
            const newCard = createOrderElement(order);
            newCard.classList.add('card-enter'); // Animation class
            
            // Insert at correct position
            const allCards = Array.from(container.querySelectorAll('.order-card'));
            if (index < allCards.length) {
                container.insertBefore(newCard, allCards[index]);
            } else {
                container.appendChild(newCard);
            }
            
            // Remove animation class after complete
            setTimeout(() => newCard.classList.remove('card-enter'), 300);
            
            // Start timer for pending orders
            if (order.status === 'PENDING') {
                startTimer(order.orderId, order.createdAt);
            }
        }
    });
    
    // Remove cards that no longer exist
    existingCardsMap.forEach((existing, orderId) => {
        if (!processedOrderIds.has(orderId)) {
            const card = existing.element;
            card.style.opacity = '0';
            card.style.transform = 'scale(0.9)';
            setTimeout(() => card.remove(), 300);
            
            // Stop timer
            if (timers[orderId]) {
                clearInterval(timers[orderId]);
                delete timers[orderId];
            }
        }
    });
}

// Create order card element
function createOrderElement(order) {
    const div = document.createElement('div');
    div.className = `order-card ${order.status.toLowerCase()}`;
    div.dataset.orderId = order.orderId; // Để tracking
    div.dataset.status = order.status;   // Để so sánh
    div.innerHTML = createOrderCard(order);
    return div;
}

// Create order card HTML
function createOrderCard(order) {
    const statusClass = order.status.toLowerCase();
    const statusIcon = getStatusIcon(order.status);
    const statusText = getStatusText(order.status);
    
    return `
        <div class="order-card ${statusClass}">
            <div class="order-header">
                <div class="order-id">
                    <i class="fas fa-hashtag"></i> ${order.orderId}
                </div>
                <span class="order-status ${statusClass}">
                    ${statusIcon} ${statusText}
                </span>
            </div>
            
            <div class="order-info">
                <div class="info-item">
                    <i class="fas fa-user"></i>
                    <span class="info-label">Khách hàng:</span>
                    <span class="info-value">${order.userId}</span>
                </div>
                <div class="info-item">
                    <i class="fas fa-money-bill-wave"></i>
                    <span class="info-label">Số tiền:</span>
                    <span class="info-value">${formatCurrency(order.amount)}</span>
                </div>
                <div class="info-item">
                    <i class="fas fa-clock"></i>
                    <span class="info-label">Tạo lúc:</span>
                    <span class="info-value">${formatDateTime(order.createdAt)}</span>
                </div>
                ${order.cancelledAt ? `
                <div class="info-item">
                    <i class="fas fa-times-circle"></i>
                    <span class="info-label">Hủy lúc:</span>
                    <span class="info-value">${formatDateTime(order.cancelledAt)}</span>
                </div>
                ` : ''}
            </div>
            
            ${order.status === 'PENDING' ? `
                <div class="order-timer" id="timer-${order.orderId}">
                    <div class="timer-icon">
                        <i class="fas fa-hourglass-half"></i>
                    </div>
                    <div class="timer-content">
                        <div class="timer-label">Thời gian còn lại</div>
                        <div class="timer-value" id="timer-value-${order.orderId}">--:--</div>
                    </div>
                </div>
            ` : ''}
            
            ${order.cancelReason ? `
                <div class="info-item" style="margin-bottom: 16px;">
                    <i class="fas fa-info-circle"></i>
                    <span class="info-label">Lý do:</span>
                    <span class="info-value">${order.cancelReason}</span>
                </div>
            ` : ''}
            
            ${order.status === 'PENDING' ? `
                <div class="order-actions">
                    <button class="btn btn-success btn-sm" onclick="payOrder('${order.orderId}')">
                        <i class="fas fa-credit-card"></i> Thanh toán
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="cancelOrder('${order.orderId}')">
                        <i class="fas fa-times"></i> Hủy đơn
                    </button>
                </div>
            ` : ''}
        </div>
    `;
}

// Start countdown timer (improved - không restart nếu đã chạy)
function startTimer(orderId, createdAt) {
    // Nếu timer đã chạy, không tạo lại
    if (timers[orderId]) {
        return;
    }
    
    const TTL_MS = 15 * 60 * 1000; // 15 minutes
    const createdTime = new Date(createdAt).getTime();
    
    // Update immediately first
    updateTimerDisplay(orderId, createdTime, TTL_MS);
    
    // Then update every second
    timers[orderId] = setInterval(() => {
        updateTimerDisplay(orderId, createdTime, TTL_MS);
    }, 1000);
}

// Update timer display (helper function)
function updateTimerDisplay(orderId, createdTime, TTL_MS) {
    const now = Date.now();
    const elapsed = now - createdTime;
    const remaining = TTL_MS - elapsed;
    
    const timerElement = document.getElementById(`timer-${orderId}`);
    const timerValueElement = document.getElementById(`timer-value-${orderId}`);
    
    if (!timerElement || !timerValueElement) {
        if (timers[orderId]) {
            clearInterval(timers[orderId]);
            delete timers[orderId];
        }
        return;
    }
    
    if (remaining <= 0) {
        timerValueElement.textContent = 'HẾT HẠN';
        timerElement.classList.add('timer-expired');
        
        if (timers[orderId]) {
            clearInterval(timers[orderId]);
            delete timers[orderId];
        }
        
        // Refresh to get updated status (only once)
        setTimeout(refreshOrders, 2000);
        return;
    }
    
    const minutes = Math.floor(remaining / 60000);
    const seconds = Math.floor((remaining % 60000) / 1000);
    timerValueElement.textContent = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    
    // Change color when less than 2 minutes
    if (remaining < 2 * 60 * 1000) {
        timerElement.classList.add('timer-expired');
    } else {
        timerElement.classList.remove('timer-expired');
    }
}

// Pay order
async function payOrder(orderId) {
    if (!confirm(`Xác nhận thanh toán đơn hàng ${orderId}?`)) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/${orderId}/pay`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('success', 'Thành công', `Đã thanh toán đơn hàng ${orderId}`);
            
            // Clear timer
            if (timers[orderId]) {
                clearInterval(timers[orderId]);
                delete timers[orderId];
            }
            
            await refreshOrders();
        } else {
            showToast('error', 'Lỗi', data.message || 'Không thể thanh toán');
        }
    } catch (error) {
        console.error('Error paying order:', error);
        showToast('error', 'Lỗi', 'Không thể kết nối đến server');
    } finally {
        showLoading(false);
    }
}

// Cancel order
async function cancelOrder(orderId) {
    if (!confirm(`Xác nhận hủy đơn hàng ${orderId}?`)) {
        return;
    }
    
    showLoading(true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/${orderId}/cancel`, {
            method: 'POST'
        });
        
        const data = await response.json();
        
        if (data.success) {
            showToast('warning', 'Đã hủy', `Đơn hàng ${orderId} đã bị hủy`);
            
            // Clear timer
            if (timers[orderId]) {
                clearInterval(timers[orderId]);
                delete timers[orderId];
            }
            
            await refreshOrders();
        } else {
            showToast('error', 'Lỗi', data.message || 'Không thể hủy đơn hàng');
        }
    } catch (error) {
        console.error('Error canceling order:', error);
        showToast('error', 'Lỗi', 'Không thể kết nối đến server');
    } finally {
        showLoading(false);
    }
}

// Clear all orders (for demo purposes)
async function clearAllOrders() {
    if (!confirm('Xác nhận xóa TẤT CẢ đơn hàng? (Chỉ dùng để demo)')) {
        return;
    }
    
    // Clear all timers
    Object.keys(timers).forEach(orderId => {
        clearInterval(timers[orderId]);
    });
    timers = {};
    
    orders = {};
    renderOrders();
    updateStatistics();
    
    showToast('info', 'Đã xóa', 'Đã xóa tất cả đơn hàng');
}

// Update statistics
function updateStatistics() {
    const orderList = Object.values(orders);
    
    const pendingCount = orderList.filter(o => o.status === 'PENDING').length;
    const paidCount = orderList.filter(o => o.status === 'PAID').length;
    const cancelledCount = orderList.filter(o => o.status === 'CANCELLED').length;
    
    document.getElementById('stat-pending').textContent = pendingCount;
    document.getElementById('stat-paid').textContent = paidCount;
    document.getElementById('stat-cancelled').textContent = cancelledCount;
}

// Helper functions
function getStatusIcon(status) {
    const icons = {
        'PENDING': '<i class="fas fa-hourglass-half"></i>',
        'PAID': '<i class="fas fa-check-circle"></i>',
        'CANCELLED': '<i class="fas fa-times-circle"></i>'
    };
    return icons[status] || '';
}

function getStatusText(status) {
    const texts = {
        'PENDING': 'Chờ thanh toán',
        'PAID': 'Đã thanh toán',
        'CANCELLED': 'Đã hủy'
    };
    return texts[status] || status;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDateTime(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

// Show toast notification
function showToast(type, title, message) {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = {
        'success': '<i class="fas fa-check-circle"></i>',
        'error': '<i class="fas fa-exclamation-circle"></i>',
        'warning': '<i class="fas fa-exclamation-triangle"></i>',
        'info': '<i class="fas fa-info-circle"></i>'
    }[type] || '';
    
    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-content">
            <div class="toast-title">${title}</div>
            <div class="toast-message">${message}</div>
        </div>
    `;
    
    container.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        toast.style.animation = 'toastSlideIn 0.3s ease-out reverse';
        setTimeout(() => toast.remove(), 300);
    }, 5000);
}

// Show/hide loading overlay
function showLoading(show) {
    const overlay = document.getElementById('loading-overlay');
    overlay.style.display = show ? 'flex' : 'none';
}

// Cleanup on page unload
window.addEventListener('beforeunload', function() {
    // Clear all intervals
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
    }
    
    Object.keys(timers).forEach(orderId => {
        clearInterval(timers[orderId]);
    });
});
