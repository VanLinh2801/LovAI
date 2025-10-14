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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private EditText edtEmail, edtPassword;
    private Button loginButton, googleButton;
    private UserApi userApi;
    private TextView tvSignUp;
//    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.editTextText);
        edtPassword = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.button4);
        googleButton = findViewById(R.id.button);
        userApi = RetrofitClient.getUserApi(this);

        //login google
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleButton.setOnClickListener(v -> signInWithGoogle());


        //login backend
        loginButton.setOnClickListener(v->{
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            LoginRequest request = new LoginRequest(email, password);
            userApi.login(request).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        LoginResponse loginRes = response.body();
//                        Luu thong tin
                        SharedPreferences prefs  =getSharedPreferences("MyAppPrefs",MODE_PRIVATE);
                        SharedPreferences.Editor editor=prefs.edit();
                        editor.putString("userId",loginRes.getUserId());
                        editor.putString("token",loginRes.getToken());
                        editor.putString("email",loginRes.getEmail());
                        editor.putString("name",loginRes.getName());
                        editor.apply();


                        Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

//        SignUp
        tvSignUp=findViewById(R.id.textView8);
        SignUpRedirect();

//        ForgotPassword
//        tvForgotPassword=findViewById(R.id.textView14);

    }
    private void SignUpRedirect(){
        tvSignUp.setOnClickListener(v->{
            Intent intent=new Intent(LoginActivity.this,SignUp.class);
            startActivity(intent);
            finish();
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null); // tạo firebase credential
        mAuth.signInWithCredential(credential)  // gui credential -> firebase auth
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Lưu thông tin user vào SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", user.getEmail());
                            editor.putString("name", user.getDisplayName());
                            editor.putString("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "Google Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Firebase auth failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }



//    private void ForgotPassword(){
////        tvForgotPassword.setOnClickListener(v->{
////            Intent intent = new Intent(Fo)
////        });
//    }
}