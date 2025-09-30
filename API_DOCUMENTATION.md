# LovAI API Documentation

## Authentication API

### 1. Login
**POST** `/api/v1/users/login`

Đăng nhập và nhận JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "email": "user@example.com",
  "name": "User Name"
}
```

### 2. Logout
**POST** `/api/v1/users/logout`

Đăng xuất (client cần xóa token).

**Response:**
```json
{
  "message": "Đăng xuất thành công"
}
```

## Protected Endpoints

Tất cả các endpoint khác (trừ register, verify-email, login) đều yêu cầu authentication.

**Header cần thiết:**
```
Authorization: Bearer <your-jwt-token>
```

## Cách sử dụng JWT

1. **Login** để nhận token
2. **Thêm token vào header** cho các request được bảo vệ:
   ```
   Authorization: Bearer <token>
   ```
3. **Logout** - chỉ cần xóa token ở client

## JWT Configuration

- **Secret Key**: `lovai-secret-key-2024-very-secure-key-for-jwt-token-generation`
- **Expiration**: 24 hours (86400000 milliseconds)
- **Algorithm**: HMAC256

## Error Responses

### 401 Unauthorized
```json
{
  "message": "Email hoặc mật khẩu không đúng"
}
```

### 400 Bad Request
```json
{
  "message": "Tài khoản chưa được xác minh"
}
```
