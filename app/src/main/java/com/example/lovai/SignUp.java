package com.example.lovai;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lovai.API.RetrofitClient;
import com.example.lovai.API.UserApi;
import com.example.lovai.DTO.UserRegisterRequest;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUp extends AppCompatActivity {
    private EditText edtName, edtEmail, edtPassword, edtDateofBirth;
    private Button btnRegister;
    private UserApi userApi;

    private Spinner genderSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        userApi = RetrofitClient.getUserApi(this);

//        Calendar fix
        edtDateofBirth = findViewById(R.id.editTextTextDateOfBirth);
        Dateofbirth();


//        SignUp
        edtName = findViewById(R.id.editTextText2);
        edtEmail = findViewById(R.id.editTextTextEmailAddress);
        edtPassword = findViewById(R.id.editTextTextPassword2);
        genderSpinner=findViewById(R.id.spinnerGender);
        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this,R.array.gender_options,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);
        btnRegister = findViewById(R.id.buttonRegister);

        btnRegister.setOnClickListener(v->{
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            String gender = genderSpinner.getSelectedItem().toString();
            String dob = edtDateofBirth.getText().toString();
            UserRegisterRequest userRegisterRequest = new UserRegisterRequest(email, password, name, gender, dob);
            userApi.register(userRegisterRequest).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        String email1 = email;
                        Intent intent = new Intent(SignUp.this, VerifyEmail.class);
                        intent.putExtra("email", email1);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Toast.makeText(SignUp.this, "Sign up failed!", Toast.LENGTH_SHORT).show();
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
                        String dob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        edtDateofBirth.setText(dob);
                    },
                    year, month, day // ngày hiện tại
            );
            datePickerDialog.show();
        });
    }

}