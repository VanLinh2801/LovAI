# Cloud Service Documentation

## Tổng quan

CloudService là một interface để quản lý việc upload, download và xóa file trên cloud storage. Hiện tại được implement với Supabase Storage.

## Cấu hình

### Application Properties
```properties
# Supabase Configuration
supabase.url=https://bqvmudyaaflksxwjbsxy.supabase.co
supabase.key=your-supabase-service-key
supabase.bucket=media-lovai
```

## Interface CloudService

### Các method chính:

#### 1. Upload File
```java
String uploadFile(MultipartFile file, String folder)
```
- Upload một file lên cloud storage
- `folder`: Thư mục đích (ví dụ: "users", "memories", "couples")
- Trả về: URL public của file đã upload

#### 2. Upload Multiple Files
```java
List<String> uploadMultipleFiles(List<MultipartFile> files, String folder)
```
- Upload nhiều file cùng lúc
- Trả về: Danh sách URL của các file đã upload

#### 3. Delete File
```java
boolean deleteFile(String fileUrl)
```
- Xóa file khỏi cloud storage
- Trả về: true nếu xóa thành công

#### 4. Delete Multiple Files
```java
boolean deleteMultipleFiles(List<String> fileUrls)
```
- Xóa nhiều file cùng lúc
- Trả về: true nếu tất cả file được xóa thành công

#### 5. File Operations
```java
boolean fileExists(String fileUrl)           // Kiểm tra file tồn tại
FileInfo getFileInfo(String fileUrl)        // Lấy thông tin file
String getPublicUrl(String fileUrl)         // Lấy URL public
String getSignedUrl(String fileUrl, int expiresInSeconds)  // Tạo signed URL
```

## File Validation

### Supported Formats:
- **Images**: JPEG, JPG, PNG, GIF, WebP
- **Videos**: MP4, AVI, MOV, WMV, WebM

### File Size Limits:
- **Images**: Tối đa 10MB
- **Videos**: Tối đa 100MB

## Cách sử dụng trong Service khác

### 1. Inject CloudService
```java
@Service
public class YourService {
    
    @Autowired
    private CloudService cloudService;
    
    public String uploadUserAvatar(MultipartFile file, UUID userId) {
        String folder = "users/" + userId + "/avatars";
        return cloudService.uploadFile(file, folder);
    }
    
    public List<String> uploadMemoryPhotos(List<MultipartFile> files, UUID memoryId) {
        String folder = "memories/" + memoryId + "/photos";
        return cloudService.uploadMultipleFiles(files, folder);
    }
}
```

### 2. Exception Handling
```java
try {
    String fileUrl = cloudService.uploadFile(file, folder);
    // Xử lý thành công
} catch (CloudStorageException e) {
    // Xử lý lỗi upload
    log.error("Upload failed: " + e.getMessage());
}
```

## File Structure trên Supabase

```
media-lovai/
├── users/
│   ├── {userId}/
│   │   ├── avatars/
│   │   └── photos/
├── memories/
│   ├── {memoryId}/
│   │   ├── photos/
│   │   └── videos/
└── couples/
    ├── {coupleId}/
    │   ├── photos/
    │   └── videos/
```

## URL Format

### Public URLs:
```
https://bqvmudyaaflksxwjbsxy.supabase.co/storage/v1/object/public/media-lovai/users/123/avatars/1234567890_abc123.jpg
```

### Signed URLs (có thời hạn):
```
https://bqvmudyaaflksxwjbsxy.supabase.co/storage/v1/object/sign/media-lovai/users/123/avatars/1234567890_abc123.jpg?token=...
```

## Error Handling

### CloudStorageException
- Được throw khi có lỗi trong quá trình upload/download/delete
- Chứa thông báo lỗi chi tiết

### Common Errors:
- `File không được để trống`
- `File không được hỗ trợ. Chỉ chấp nhận ảnh và video`
- `Không thể upload file: [error details]`
- `Lỗi đọc file: [error details]`

## Best Practices

1. **Folder Structure**: Sử dụng cấu trúc thư mục rõ ràng
2. **File Naming**: File được tự động đặt tên unique với timestamp
3. **Validation**: Luôn validate file trước khi upload
4. **Error Handling**: Luôn handle CloudStorageException
5. **Cleanup**: Xóa file cũ khi upload file mới
6. **Security**: Sử dụng signed URLs cho file nhạy cảm
