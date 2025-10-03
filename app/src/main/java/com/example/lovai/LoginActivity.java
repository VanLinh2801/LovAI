package com.example.lovai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lovai.API.RetrofitClient;
import com.example.lovai.API.UserApi;
import com.example.lovai.DTO.LoginRequest;
import com.example.lovai.DTO.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private Button loginButton;
    private UserApi userApi;
    private TextView tvSignUp;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.editTextText);
        edtPassword = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button4);

        userApi = RetrofitClient.getUserApi();

        loginButton.setOnClickListener(v->{
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            LoginRequest request = new LoginRequest(email, password);
            userApi.login(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response.isSuccessful() && response.body() != null) {
//                        LoginResponse loginRes = response.body();

                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

//        SignUp
        tvSignUp=findViewById(R.id.textView8);
        SignUpRedirect();

//        ForgotPassword
        tvForgotPassword=findViewById(R.id.textView14);

    }
    private void SignUpRedirect(){
        tvSignUp.setOnClickListener(v->{
            Intent intent=new Intent(LoginActivity.this,SignUp.class);
            startActivity(intent);
            finish();
        });
    }

    private void ForgotPassword(){

    }
}