package com.example.lovai.API;

import com.example.lovai.DTO.CoupleResponse;
import com.example.lovai.DTO.CreateCoupleWithPartnerRequest;
import com.example.lovai.DTO.UpdateCoupleRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CoupleApi {
    @GET("api/v1/couples/me")
    Call<CoupleResponse> getMyCouple(@Query("userId") String userId);

    @PUT("api/v1/couples/{id}")
    Call<CoupleResponse> updateCouple(@Path("id") String coupleId, @Body UpdateCoupleRequest request);

    @POST("api/v1/couples/with-partner")
    Call<CoupleResponse> createCoupleWithPartner(@Body CreateCoupleWithPartnerRequest request);
}
