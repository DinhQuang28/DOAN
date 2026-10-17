document.addEventListener("DOMContentLoaded", function() {
    
    // 1. Sidebar Toggle for Mobile & Responsive
    const sidebarToggleBtn = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    
    if (sidebarToggleBtn && sidebar) {
        sidebarToggleBtn.addEventListener('click', function(e) {
            e.preventDefault();
            sidebar.classList.toggle('show');
        });

        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', function(e) {
            if (window.innerWidth < 992 && sidebar.classList.contains('show')) {
                if (!sidebar.contains(e.target) && !sidebarToggleBtn.contains(e.target)) {
                    sidebar.classList.remove('show');
                }
            }
        });
    }

    // 2. Realtime Header Clock
    const headerClock = document.getElementById('headerClock');
    if (headerClock) {
        function updateClock() {
            const now = new Date();
            const days = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
            const dayName = days[now.getDay()];
            const dateStr = String(now.getDate()).padStart(2, '0') + '/' + 
                            String(now.getMonth() + 1).padStart(2, '0') + '/' + 
                            now.getFullYear();
            const timeStr = String(now.getHours()).padStart(2, '0') + ':' + 
                            String(now.getMinutes()).padStart(2, '0') + ':' + 
                            String(now.getSeconds()).padStart(2, '0');
            headerClock.textContent = dayName + ', ' + dateStr + ' ' + timeStr;
        }
        updateClock();
        setInterval(updateClock, 1000);
    }

    // 3. Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            try {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            } catch (err) {}
        }, 5000);
    });

    // 4. Confirmation dialogs for dangerous actions
    const deleteForms = document.querySelectorAll('.delete-form, .confirm-action');
    deleteForms.forEach(function(form) {
        form.addEventListener('submit', function(e) {
            const confirmMsg = form.getAttribute('data-confirm') || 'Bạn có chắc chắn muốn thực hiện thao tác này?';
            if (!confirm(confirmMsg)) {
                e.preventDefault();
            }
        });
    });

    // 5. WebSocket Configuration for Live Room Status
    let stompClient = null;

    function connectWebSocket() {
        if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
            const socket = new SockJS('/ws');
            stompClient = Stomp.over(socket);
            stompClient.debug = null; // Disable noisy logs
            
            stompClient.connect({}, function(frame) {
                // Subscribe to room status updates
                stompClient.subscribe('/topic/rooms', function(message) {
                    try {
                        const roomUpdate = JSON.parse(message.body);
                        updateRoomStatusUI(roomUpdate);
                    } catch (e) {
                        console.error('Error parsing room update:', e);
                    }
                });
            }, function(error) {
                // Try to reconnect after 6 seconds
                setTimeout(connectWebSocket, 6000);
            });
        }
    }

    function updateRoomStatusUI(data) {
        const roomElement = document.getElementById('room-' + data.roomId);
        if (roomElement) {
            // Remove old status classes
            roomElement.classList.remove(
                'room-status-available', 'room-status-occupied', 'room-status-cleaning', 'room-status-maintenance',
                'bg-success', 'bg-danger', 'bg-warning', 'bg-secondary'
            );
            
            let statusClass = 'room-status-available';
            if (data.status === 'OCCUPIED') statusClass = 'room-status-occupied';
            else if (data.status === 'CLEANING') statusClass = 'room-status-cleaning';
            else if (data.status === 'MAINTENANCE') statusClass = 'room-status-maintenance';

            roomElement.classList.add(statusClass);
        }
    }

    // Connect websocket if room map or dashboard is active
    if (document.getElementById('mini-room-map') || document.querySelector('.room-grid') || document.getElementById('room-map-container')) {
        connectWebSocket();
    }
});
