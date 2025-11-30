package com.example.lovai.API;
import com.example.lovai.DTO.ChangePasswordRequest;
import com.example.lovai.DTO.LoginRequest;
import com.example.lovai.DTO.LoginResponse;
import com.example.lovai.DTO.User.UserUpdateRequest;
import com.example.lovai.DTO.UserRegisterRequest;
import com.example.lovai.DTO.User.UserResponse;
import com.example.lovai.DTO.VerifyEmailRequest;

import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {
    @POST("api/v1/users/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/users/register")
    Call<Map<String,String>> register(@Body UserRegisterRequest request);

    @POST("api/v1/users/verify-email")
    Call<Map<String,String>> verifyEmail(@Body VerifyEmailRequest request);

    @POST("api/v1/users/forgot-password")
    Call<Map<String,String>> forgotPassword(@Body LoginRequest request);

    @PUT("api/v1/users/{id}/password")
    Call<Map<String, String>> changePassword(@Path("id") UUID id, @Body ChangePasswordRequest request);

    // Đăng xuất
    @POST("api/v1/users/logout")
    Call<Map<String, String>> logout();

    @GET("api/v1/users/{id}")
    Call<UserResponse> getUserById(@Path("id") String id);

    @PUT("api/v1/users/{id}")
    Call<UserResponse> updateUser(@Path("id") String id, @Body UserUpdateRequest request);


}
