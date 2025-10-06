# WebSocket Notification System Guide

## Tổng quan

Hệ thống WebSocket notification cho phép gửi realtime notifications đến users thông qua WebSocket connection với JWT authentication.

## Kiến trúc hệ thống

### 1. **WebSocketConfig.java**
- Cấu hình WebSocket message broker
- Endpoint: `/ws/notifications` với SockJS fallback
- Application prefix: `/app`
- Broker prefixes: `/topic`, `/queue`
- User destination prefix: `/user`

### 2. **WebSocketAuthInterceptor.java**
- Xử lý JWT authentication cho WebSocket connections
- Đọc Authorization header từ CONNECT message
- Parse JWT token và set Principal với userId
- Reject connection nếu token không hợp lệ

### 3. **NotificationWebSocketController.java**
- Controller để gửi messages qua WebSocket
- Methods:
  - `sendToUser()` - Gửi đến user cụ thể
  - `sendToUsers()` - Gửi đến nhiều users
  - `broadcastToAll()` - Broadcast đến tất cả users
  - `sendToCategory()` - Gửi đến users theo category

### 4. **DeliveryService.java** (Updated)
- Tích hợp WebSocket để gửi realtime notifications
- Gửi NotificationResponse DTO qua WebSocket
- Kết hợp với database storage và event logging

## Cách kết nối từ Client

### JavaScript/TypeScript Example:

```javascript
// Kết nối WebSocket với JWT authentication
const token = localStorage.getItem('jwt_token');
const socket = new SockJS('/ws/notifications');

const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': `Bearer ${token}`
}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe để nhận notifications
    stompClient.subscribe('/user/queue/notifications', function(notification) {
        const notificationData = JSON.parse(notification.body);
        console.log('Received notification:', notificationData);
        
        // Hiển thị notification trong UI
        showNotification(notificationData);
    });
    
    // Subscribe để nhận broadcast notifications
    stompClient.subscribe('/topic/notifications', function(notification) {
        const notificationData = JSON.parse(notification.body);
        console.log('Broadcast notification:', notificationData);
    });
    
}, function(error) {
    console.log('Connection error: ' + error);
});
```

### React Example:

```jsx
import { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const NotificationComponent = () => {
    const [notifications, setNotifications] = useState([]);
    const [stompClient, setStompClient] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('jwt_token');
        const socket = new SockJS('/ws/notifications');
        const client = Stomp.over(socket);

        client.connect({
            'Authorization': `Bearer ${token}`
        }, () => {
            console.log('WebSocket connected');
            setStompClient(client);
            
            // Subscribe to user notifications
            client.subscribe('/user/queue/notifications', (message) => {
                const notification = JSON.parse(message.body);
                setNotifications(prev => [notification, ...prev]);
            });
        });

        return () => {
            if (client.connected) {
                client.disconnect();
            }
        };
    }, []);

    return (
        <div>
            {notifications.map(notification => (
                <div key={notification.id} className="notification">
                    <h3>{notification.title}</h3>
                    <p>{notification.body}</p>
                </div>
            ))}
        </div>
    );
};
```

## API Endpoints

### WebSocket Endpoints:
- **Connection**: `ws://localhost:8080/ws/notifications`
- **User notifications**: `/user/{userId}/queue/notifications`
- **Broadcast**: `/topic/notifications`
- **Category notifications**: `/topic/notifications/{category}`

### REST API Endpoints:
- `GET /api/v1/notifications` - Lấy danh sách notifications
- `GET /api/v1/notifications/unread-count` - Đếm số notifications chưa đọc
- `POST /api/v1/notifications/mark-as-read` - Đánh dấu đã đọc
- `POST /api/v1/notifications/mark-all-as-read` - Đánh dấu tất cả đã đọc

## Message Format

### NotificationResponse DTO:
```json
{
    "id": "uuid",
    "templateCode": "welcome",
    "channel": "IN_APP",
    "title": "Chào mừng đến với LovAI!",
    "body": "Cảm ơn bạn đã đăng ký tài khoản.",
    "payload": {
        "action": "navigate",
        "url": "/welcome"
    },
    "category": "welcome",
    "createdById": "uuid",
    "createdByName": "Admin",
    "status": "SENT",
    "scheduledAt": "2024-01-01T10:00:00Z",
    "sentAt": "2024-01-01T10:00:01Z",
    "createdAt": "2024-01-01T09:59:00Z",
    "readAt": null,
    "isRead": false
}
```

## Authentication Flow

1. **Client gửi CONNECT message** với Authorization header
2. **WebSocketAuthInterceptor** xử lý:
   - Đọc Authorization header
   - Validate JWT token
   - Extract userId và email
   - Set Principal với user info
3. **Connection được accept** nếu token hợp lệ
4. **Client có thể subscribe** đến các destinations

## Error Handling

### Connection Errors:
- **Invalid JWT**: Connection bị reject
- **Expired token**: Connection bị reject
- **Missing Authorization header**: Connection bị reject

### Message Errors:
- **User not found**: Log error, skip delivery
- **WebSocket connection lost**: Retry logic
- **Delivery failure**: Update status, log event

## Security Considerations

1. **JWT Validation**: Mọi connection đều phải có JWT hợp lệ
2. **User Isolation**: Users chỉ nhận được notifications của mình
3. **CORS Configuration**: Cấu hình allowed origins
4. **Rate Limiting**: Có thể thêm rate limiting cho WebSocket connections

## Monitoring & Logging

- **Connection events**: Log khi user connect/disconnect
- **Message delivery**: Log success/failure
- **Authentication**: Log authentication attempts
- **Performance**: Monitor connection count, message throughput

## Testing

### Unit Tests:
- Test WebSocketAuthInterceptor với valid/invalid tokens
- Test NotificationWebSocketController message sending
- Test DeliveryService integration

### Integration Tests:
- Test full WebSocket flow từ connection đến message delivery
- Test authentication và authorization
- Test error scenarios

## Deployment Notes

1. **Load Balancing**: Cần sticky sessions cho WebSocket
2. **Scaling**: Có thể cần Redis để share WebSocket state
3. **Health Checks**: Monitor WebSocket connection health
4. **SSL/TLS**: Sử dụng WSS cho production

