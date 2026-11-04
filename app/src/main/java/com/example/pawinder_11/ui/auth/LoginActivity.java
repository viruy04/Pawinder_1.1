package com.example.pawinder_11.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pawinder_11.R;
import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.LoginRequest;
import com.example.pawinder_11.model.User;
import com.example.pawinder_11.ui.main.MainActivity;
import com.example.pawinder_11.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etLogin;
    private EditText etPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String nickname = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (nickname.isEmpty()) {
            etLogin.setError("Введите логин");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Введите пароль");
            return;
        }

        // сервер на Render может "просыпаться" до 40 секунд — блокируем повторные нажатия
        btnLogin.setEnabled(false);

        ApiClient.getApi().login(new LoginRequest(nickname, password))
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        btnLogin.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            SessionManager.save(LoginActivity.this, response.body());
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else if (response.code() == 401) {
                            toast("Неверный логин или пароль");
                        } else {
                            toast("Ошибка сервера: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        toast("Нет связи с сервером");
                    }
                });
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
