package com.example.lovai;

import android.app.DatePickerDialog;
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
import com.example.lovai.DTO.UserRegisterRequest;

import java.util.Calendar;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {
    private EditText edtName, edtEmail, edtPassword, edtGender, edtDateofBirth;
    private Button btnRegister;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        userApi = RetrofitClient.getUserApi();

//        Calendar fix
        edtDateofBirth = findViewById(R.id.editTextTextDateOfBirth);
        Dateofbirth();



//        SignUp
        edtName = findViewById(R.id.editTextText2);
        edtEmail = findViewById(R.id.editTextTextEmailAddress);
        edtPassword = findViewById(R.id.editTextTextPassword2);
        edtGender = findViewById(R.id.editTextTextGender);
        btnRegister = findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(v->{
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            String gender = edtGender.getText().toString();
            String dob = edtDateofBirth.getText().toString();
            UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, password, name, gender, dob);
            userApi.register(userRegisterRequest).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        Map<String, String> body = response.body();
                        String email = body.get("email");
                        Intent intent = new Intent(SignUp.this, VerifyEmail.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(SignUp.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });

        });
    }
    public void Dateofbirth(){
        edtDateofBirth.setOnClickListener(v->{
            // Lấy ngày hiện tại làm mặc định
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SignUp.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Gán giá trị ngày tháng vào EditText
                        String dob = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        edtDateofBirth.setText(dob);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

}