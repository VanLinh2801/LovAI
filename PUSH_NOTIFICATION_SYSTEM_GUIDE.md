# Push Notification System Guide

## Tổng quan

Hệ thống Push Notification với Firebase Cloud Messaging (FCM) đã được tích hợp hoàn chỉnh, bao gồm cả WebSocket và Push notification với logic thông minh để chọn kênh phù hợp.

## Kiến trúc hệ thống

### 1. **PushTokenRepository.java** - Repository cho Push Tokens
- `findByUserIdAndIsActiveTrue()` - Tìm tokens active của user
- `findByToken()` - Tìm token theo string
- `findByUserIdAndPlatformAndIsActiveTrue()` - Tìm tokens theo platform
- `countByUserIdAndIsActiveTrue()` - Đếm số tokens active
- `findInactiveTokensOlderThan()` - Tìm tokens cũ để cleanup

### 2. **PushTokenService.java** - Service quản lý Push Tokens
- `createOrUpdateToken()` - Tạo/cập nhật push token
- `deactivateToken()` - Vô hiệu hóa token
- `updateLastSeenAt()` - Cập nhật thời gian hoạt động cuối
- `getActiveTokensByUserId()` - Lấy tokens active của user
- `cleanupInactiveTokens()` - Dọn dẹp tokens cũ
- `deactivateAllTokensByUserId()` - Vô hiệu hóa tất cả tokens của user

### 3. **PushTokenController.java** - REST API cho Push Tokens
- `POST /api/v1/push-tokens` - Đăng ký push token
- `DELETE /api/v1/push-tokens/{token}` - Vô hiệu hóa token
- `PUT /api/v1/push-tokens/update-last-seen` - Cập nhật last seen
- `DELETE /api/v1/push-tokens/user/all` - Vô hiệu hóa tất cả tokens của user

### 4. **FirebaseConfig.java** - Cấu hình Firebase
- Đọc Firebase credentials từ file JSON hoặc environment variables
- Khởi tạo FirebaseApp và FirebaseMessaging
- Hỗ trợ cả development và production environments

### 5. **PushService.java** - Service gửi FCM Notifications
- `sendToUser()` - Gửi push notification đến user
- `sendToUsers()` - Gửi đến nhiều users
- `sendToUserByPlatform()` - Gửi theo platform cụ thể
- `sendTestNotification()` - Test push notification
- Tích hợp với NotificationEventLogger để ghi log events

### 6. **DeliveryService.java** (Updated) - Logic thông minh
- Kiểm tra user online/offline qua SimpUserRegistry
- **User Online**: Gửi qua WebSocket (realtime)
- **User Offline**: Gửi qua Push notification (FCM)
- Tránh gửi trùng lặp giữa 2 kênh
- Event logging cho cả 2 kênh

## API Endpoints

### Push Token Management:
```
POST /api/v1/push-tokens
Headers: X-User-Id: {userId}
Body: {
  "token": "fcm_token_string",
  "platform": "android|ios|web",
  "deviceId": "optional_device_id"
}

DELETE /api/v1/push-tokens/{token}
PUT /api/v1/push-tokens/update-last-seen
DELETE /api/v1/push-tokens/user/all
```

### Notification Endpoints (existing):
```
GET /api/v1/notifications
GET /api/v1/notifications/unread-count
POST /api/v1/notifications/mark-as-read
POST /api/v1/notifications/mark-all-as-read
```

## Client Integration

### 1. **Đăng ký Push Token (Android/iOS)**

```javascript
// React Native / Flutter
const registerPushToken = async () => {
  const token = await messaging().getToken();
  
  await fetch('/api/v1/push-tokens', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`,
      'X-User-Id': userId
    },
    body: JSON.stringify({
      token: token,
      platform: 'android', // hoặc 'ios'
      deviceId: deviceId
    })
  });
};
```

### 2. **WebSocket Connection (cho realtime)**

```javascript
// Kết nối WebSocket khi user online
const connectWebSocket = () => {
  const socket = new SockJS('/ws/notifications');
  const stompClient = Stomp.over(socket);
  
  stompClient.connect({
    'Authorization': `Bearer ${jwtToken}`
  }, () => {
    // Subscribe để nhận notifications
    stompClient.subscribe('/user/queue/notifications', (message) => {
      const notification = JSON.parse(message.body);
      showNotification(notification);
    });
  });
};
```

### 3. **Xử lý Push Notifications (Android/iOS)**

```javascript
// React Native
import messaging from '@react-native-firebase/messaging';

// Xử lý foreground messages
messaging().onMessage(async remoteMessage => {
  console.log('Foreground message:', remoteMessage);
  showInAppNotification(remoteMessage);
});

// Xử lý background messages
messaging().setBackgroundMessageHandler(async remoteMessage => {
  console.log('Background message:', remoteMessage);
});
```

## Logic Delivery

### 1. **Smart Delivery Logic**
```
User Online (WebSocket connected)
├── Gửi qua WebSocket (realtime)
└── Log: SENT, DELIVERED

User Offline (WebSocket disconnected)
├── Gửi qua Push Notification (FCM)
├── Log: PUSH_SENT, DELIVERED
└── Deactivate invalid tokens
```

### 2. **Event Logging**
- `ENQUEUED` - Notification được queue
- `SENT` - Notification được gửi
- `DELIVERED` - Notification được deliver
- `PUSH_SENT` - Push notification được gửi
- `DELIVERY_FAILED` - Delivery thất bại
- `READ` - User đọc notification

## Configuration

### 1. **Environment Variables**
```properties
# Firebase configuration
firebase.config.path=/path/to/firebase-service-account.json

# JWT configuration
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### 2. **Firebase Setup**
1. Tạo Firebase project
2. Enable Cloud Messaging
3. Download service account JSON
4. Set `firebase.config.path` environment variable

### 3. **Dependencies**
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

## Testing

### 1. **Test Push Token Registration**
```bash
curl -X POST http://localhost:8080/api/v1/push-tokens \
  -H "Content-Type: application/json" \
  -H "X-User-Id: {userId}" \
  -d '{
    "token": "test_fcm_token",
    "platform": "android",
    "deviceId": "test_device"
  }'
```

### 2. **Test Notification Creation**
```java
// Trong service khác
CreateNotificationRequest request = new CreateNotificationRequest();
request.setTitle("Test Notification");
request.setBody("This is a test notification");
request.setChannel(NotifChannel.IN_APP);
request.setRecipientIds(List.of(userId));

notificationService.createNotification(request);
```

### 3. **Test Push Service**
```java
@Autowired
private PushService pushService;

// Test push notification
pushService.sendTestNotification("fcm_token", "Test Title", "Test Body");
```

## Monitoring & Analytics

### 1. **Event Tracking**
- Track delivery success/failure rates
- Monitor WebSocket vs Push usage
- Analyze user engagement

### 2. **Performance Metrics**
- Push token registration rate
- Delivery success rate
- Token cleanup efficiency
- WebSocket connection health

### 3. **Error Handling**
- Invalid FCM tokens → Auto deactivate
- WebSocket connection lost → Fallback to push
- Firebase service down → Retry logic
- User offline → Queue for later delivery

## Security Considerations

1. **Token Security**: Push tokens được lưu an toàn trong database
2. **User Authentication**: Mọi API đều yêu cầu JWT token
3. **Token Cleanup**: Tự động dọn dẹp tokens cũ/invalid
4. **Rate Limiting**: Có thể thêm rate limiting cho push notifications
5. **Data Privacy**: Không lưu sensitive data trong push payload

## Deployment Notes

1. **Firebase Credentials**: Đảm bảo service account JSON được deploy an toàn
2. **Environment Variables**: Set đúng firebase.config.path
3. **Load Balancing**: WebSocket cần sticky sessions
4. **Monitoring**: Monitor Firebase quota và usage
5. **Backup**: Backup push tokens và notification history

## Troubleshooting

### Common Issues:
1. **Firebase not initialized**: Check credentials path
2. **Push tokens invalid**: Check token format và platform
3. **WebSocket not connecting**: Check JWT token validity
4. **Notifications not delivered**: Check user online status
5. **Duplicate notifications**: Check delivery logic

### Debug Commands:
```bash
# Check Firebase status
curl -X GET http://localhost:8080/actuator/health

# Test WebSocket connection
wscat -c ws://localhost:8080/ws/notifications

# Check push token status
curl -X GET http://localhost:8080/api/v1/push-tokens/user/{userId}
```

Hệ thống Push Notification đã sẵn sàng để sử dụng với đầy đủ tính năng realtime và offline delivery! 🚀

