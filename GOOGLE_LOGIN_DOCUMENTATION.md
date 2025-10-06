# Google Login với Firebase Documentation

## Tổng quan

Chức năng "Login with Google" sử dụng Firebase Admin SDK để xác minh Firebase ID Token và tạo JWT token nội bộ LovAI.

## Cấu hình Firebase

### 1. FirebaseConfig.java
- Thêm `FirebaseAuth` bean
- Sử dụng Firebase Admin SDK để verify ID tokens

### 2. Application Properties
```properties
firebase.config.path=lovai-d5bd9-firebase-adminsdk-fbsvc-1ce77c3b86.json
```

## API Endpoint

### POST /api/v1/users/google-login

**Request Body:**
```json
{
  "firebaseIdToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2NzAyNzQ4..."
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@gmail.com",
  "name": "User Name"
}
```

## Flow xử lý

1. **Client gửi Firebase ID Token** từ Google Sign-In
2. **Server verify token** với Firebase Admin SDK
3. **Extract thông tin** (email, name) từ token
4. **Tìm hoặc tạo user** trong database
5. **Generate JWT token** LovAI và trả về

## UserService.loginWithGoogle()

### Chức năng:
- Verify Firebase ID Token
- Extract email, name từ token
- Tìm user existing hoặc tạo mới
- Set verified = true (Google login = email verified)
- Generate JWT token LovAI

### Error Handling:
- Invalid Firebase token → 401 Unauthorized
- Missing email → 400 Bad Request
- Firebase verification failed → 401 Unauthorized

## Security

- Endpoint `/api/v1/users/google-login` được permitAll()
- Firebase ID Token được verify với Firebase Admin SDK
- JWT token LovAI được generate cho authentication

## Client Integration

### 1. Frontend (React/Flutter)
```javascript
// Get Firebase ID Token
const idToken = await user.getIdToken();

// Call API
const response = await fetch('/api/v1/users/google-login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ firebaseIdToken: idToken })
});
```

### 2. Mobile (Android/iOS)
```java
// Get Firebase ID Token
FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
user.getIdToken(true).addOnCompleteListener(task -> {
    String idToken = task.getResult().getToken();
    // Call API with idToken
});
```

## Database Changes

- User table không thay đổi
- Google login users có `passwordHash = ""`
- Google login users có `verified = true`
- Tự động update name từ Google profile

## Error Responses

### 401 Unauthorized
```json
{
  "message": "Firebase token không hợp lệ: [error details]"
}
```

### 400 Bad Request
```json
{
  "message": "Email không có trong Firebase token"
}
```
