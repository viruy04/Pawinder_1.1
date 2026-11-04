package com.example.pawinder_11.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawinder_11.R;
import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.RegisterRequest;
import com.example.pawinder_11.model.User;
import com.example.pawinder_11.ui.main.MainActivity;
import com.example.pawinder_11.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private EditText etLogin;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPasswordRepeat;
    private MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        etLogin = findViewById(R.id.etLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordRepeat = findViewById(R.id.etPasswordRepeat);
        btnRegister = findViewById(R.id.btnRegister);
        MaterialButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String nickname = etLogin.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String repeat = etPasswordRepeat.getText().toString();

        if (nickname.isEmpty()) {
            etLogin.setError("Введите логин");
            return;
        }
        if (nickname.length() > 45) {
            etLogin.setError("Не больше 45 символов");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length() > 45) {
            etEmail.setError("Введите корректную почту");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            etPassword.setError("Минимум " + MIN_PASSWORD_LENGTH + " символов");
            return;
        }
        if (!password.equals(repeat)) {
            etPasswordRepeat.setError("Пароли не совпадают");
            return;
        }

        btnRegister.setEnabled(false);

        ApiClient.getApi().register(new RegisterRequest(nickname, email, password))
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        btnRegister.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            SessionManager.save(RegisterActivity.this, response.body());
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else if (response.code() == 409) {
                            toast("Логин или почта уже заняты");
                        } else if (response.code() == 400) {
                            toast("Проверьте введённые данные");
                        } else {
                            toast("Ошибка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        btnRegister.setEnabled(true);
                        toast("Нет связи с сервером");
                    }
                });
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
