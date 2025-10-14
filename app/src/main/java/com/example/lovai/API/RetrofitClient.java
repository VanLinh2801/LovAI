package com.example.lovai.API;
import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {
            // interceptor hiển thị request/response trong Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            //interceptor gắn token
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                        String token = prefs.getString("token", null);
                        Request.Builder requestBuilder = original.newBuilder();
                        if (token != null) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
    public static UserApi getUserApi(Context context) {
        return getRetrofit(context).create(UserApi.class);
    }
    public static CoupleApi getCoupleApi(Context context) {
        return getRetrofit(context).create(CoupleApi.class);
    }
    public static MemoryApi getMemoryApi(Context context) {
        return getRetrofit(context).create(MemoryApi.class);
    }





//    private static Retrofit retrofit = null;
//
//    public static Retrofit getRetrofit() {
//        if (retrofit == null) {
//
//            // interceptor để in log request/response
//            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//            // interceptor vào OkHttpClient
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(loggingInterceptor)
//                    .build();
//
//            // client này vào Retrofit
//            retrofit = new Retrofit.Builder()
//                    .baseUrl("http://10.0.2.2:8080/") // Backend Spring Boot chạy local
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .client(client)
//                    .build();
//        }
//        return retrofit;
//    }
//
//    public static UserApi getUserApi() {
//        return getRetrofit().create(UserApi.class);
//    }
//
//    public static CoupleApi getCoupleApi() {
//        return getRetrofit().create(CoupleApi.class);
//    }
}
