package com.example.lovai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lovai.API.RetrofitClient;
import com.example.lovai.API.UserApi;
import com.example.lovai.DTO.VerifyEmailRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyEmail extends AppCompatActivity {

    private EditText edtCode;
    private Button buttonVerify;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_email);
        edtCode = findViewById(R.id.edtCode);
        buttonVerify = findViewById(R.id.btnVerify);
        userApi = RetrofitClient.getUserApi();
        String email=getIntent().getStringExtra("email");


        buttonVerify.setOnClickListener(v->{
            String otp=edtCode.getText().toString().trim();
            if(otp.isEmpty()){
                Toast.makeText(VerifyEmail.this,"Vui lòng nhập mã xác thực",Toast.LENGTH_SHORT)
            };
            VerifyEmailRequest verifyEmailRequest = new VerifyEmailRequest(email,otp);
            userApi.verifyEmail(verifyEmailRequest).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if(response.isSuccessful() && response.body()!=null){
                        Toast.makeText(VerifyEmail.this, "Xác minh email thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(VerifyEmail.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(VerifyEmail.this, "Mã OTP không đúng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(VerifyEmail.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });

        });


    }
}