package com.example.lovai.API;

import com.example.lovai.DTO.Memory.MemoryCreateRequest;
import com.example.lovai.DTO.Memory.MemoryMediaCreateRequest;
import com.example.lovai.DTO.Memory.MemoryMediaResponse;
import com.example.lovai.DTO.Memory.MemoryResponse;
import com.example.lovai.DTO.Upload.UploadResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MemoryApi {

    @POST("api/v1/memories/create")
    Call<MemoryResponse> createMemory(@Body MemoryCreateRequest request);

    @GET("api/v1/memories")
    Call<List<MemoryResponse>> getMemoriesByCoupleId(@Query("coupleId") String coupleId);

    @GET("api/v1/media/memory/{memoryId}")
    Call<List<MemoryMediaResponse>> getMemoryMedia(
            @Path("memoryId") String memoryId
    );

    @Multipart
    @POST("api/v1/media/upload")
    Call<MemoryMediaResponse> uploadMedia(
            @Part MultipartBody.Part file,
            @Part("memoryId") RequestBody memoryId,
            @Part("folder")  RequestBody folder
    );

}
