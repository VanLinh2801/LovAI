package com.example.lovai.API;

public interface MediaApi {
    // Upload file lên cloud
//    @Multipart
//    @POST("/api/v1/media/upload")
//    Call<UploadResponse> uploadFile(
//            @Part MultipartBody.Part file,
//            @Part("folder") String folder
//    );
//
//    // Upload file vào memory
//    @Multipart
//    @POST("/api/v1/media/upload-to-memory")
//    Call<MemoryMediaResponse> uploadFileToMemory(
//            @Part MultipartBody.Part file,
//            @Part("memoryId") UUID memoryId,
//            @Part("folder") String folder
//    );
//
//    // Upload nhiều file
//    @Multipart
//    @POST("/api/v1/media/upload-multiple")
//    Call<UploadResponse> uploadMultipleFiles(
//            @Part List<MultipartBody.Part> files,
//            @Part("folder") String folder
//    );
//
//    @Multipart
//    @POST("/api/v1/media/upload-multiple-to-memory")
//    Call<List<MemoryMediaResponse>> uploadMultipleFilesToMemory(
//            @Part List<MultipartBody.Part> files,
//            @Part("memoryId") UUID memoryId,
//            @Part("folder") String folder
//    );
//
//    // Xóa file cloud
//    @DELETE("/api/v1/media/delete")
//    Call<Void> deleteFile(@Query("url") String fileUrl);
//
//    // Xóa media trong memory
//    @DELETE("/api/v1/media/memory-media/{mediaId}")
//    Call<Void> deleteMemoryMedia(@Path("mediaId") UUID mediaId);
//
//    // Lấy media của memory
//    @GET("/api/v1/media/memory/{memoryId}")
//    Call<List<MemoryMediaResponse>> getMemoryMedia(@Path("memoryId") UUID memoryId);
//
//    @GET("/api/v1/media/memory/{memoryId}/paginated")
//    Call<List<MemoryMediaResponse>> getMemoryMediaPaginated(
//            @Path("memoryId") UUID memoryId,
//            @Query("page") int page,
//            @Query("size") int size
//    );
//
//    // Tạo MemoryMedia từ URL
//    @POST("/api/v1/media/memory-media")
//    Call<MemoryMediaResponse> createMemoryMediaFromUrl(@Body MemoryMediaCreateRequest request);
//
//    // Kiểm tra file tồn tại
//    @GET("/api/v1/media/exists")
//    Call<Boolean> fileExists(@Query("url") String fileUrl);
//
//    // Lấy thông tin file
//    @GET("/api/v1/media/info")
//    Call<Object> getFileInfo(@Query("url") String fileUrl);
//
//    // Lấy signed URL
//    @GET("/api/v1/media/signed-url")
//    Call<Object> getSignedUrl(
//            @Query("url") String fileUrl,
//            @Query("expiresIn") int expiresIn
//    );
}
