# Environment Variables Setup

Tất cả các thông tin nhạy cảm đã được chuyển sang file `.env`. Bạn cần tạo file `.env` trong thư mục `lovai-api` với nội dung sau:

## Tạo file .env

Tạo file `.env` trong thư mục `lovai-api` và điền các giá trị sau:

```env
# Database Configuration
DB_URL=jdbc:postgresql://db.bqvmudyaaflksxwjbsxy.supabase.co:5432/postgres
DB_USER=postgres
DB_PASSWORD=RK3ovhI3BgOyBq22

# Email Configuration
SPRING_MAIL_USERNAME=vanlinh280104@gmail.com
SPRING_MAIL_PASSWORD=jlfpanvinnfrnlig

# JWT Configuration
JWT_SECRET=lovai-secret-key-2024-very-secure-key-for-jwt-token-generation

# Supabase Configuration
SUPABASE_URL=https://bqvmudyaaflksxwjbsxy.supabase.co
SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJxdm11ZHlhYWZsa3N4d2pic3h5Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1ODUzNTYwMSwiZXhwIjoyMDc0MTExNjAxfQ.mi06GYRXlY7iiBu-nmzCFG8WnWfPzr_wqRegew9_Amw
SUPABASE_BUCKET=media-lovai

# Firebase Configuration
FIREBASE_CONFIG_PATH=lovai-d5bd9-firebase-adminsdk-fbsvc-1ce77c3b86.json

# API Keys
GOONG_API_KEY=
SERPAPI_API_KEY=
OPEN_WEATHER_API_KEY=
```

## Lưu ý

- File `.env` đã được thêm vào `.gitignore` để không bị commit lên repository
- Project đã có dependency `spring-dotenv` nên sẽ tự động đọc file `.env` khi chạy ứng dụng
- Đảm bảo file `.env` nằm trong thư mục `lovai-api` (cùng cấp với `pom.xml`)

## Các biến môi trường

| Biến | Mô tả |
|------|-------|
| `DB_URL` | URL kết nối database PostgreSQL |
| `DB_USER` | Username database |
| `DB_PASSWORD` | Password database |
| `SPRING_MAIL_USERNAME` | Email để gửi mail |
| `SPRING_MAIL_PASSWORD` | Password email |
| `JWT_SECRET` | Secret key cho JWT token |
| `SUPABASE_URL` | URL Supabase project |
| `SUPABASE_KEY` | Service key của Supabase |
| `SUPABASE_BUCKET` | Tên bucket Supabase Storage |
| `FIREBASE_CONFIG_PATH` | Đường dẫn đến file Firebase config JSON |
| `GOONG_API_KEY` | API key cho Goong API |
| `SERPAPI_API_KEY` | API key cho SerpAPI |
| `OPEN_WEATHER_API_KEY` | API key cho OpenWeather API |