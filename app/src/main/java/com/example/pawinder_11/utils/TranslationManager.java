package com.example.pawinder_11.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.pawinder_11.api.DeepLClient;
import com.example.pawinder_11.model.DeepLResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslationManager {

    private static final String DEEPL_API_KEY = "bf55676e-d564-46c8-968d-75de95023f1a:fx";

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(Throwable t);
    }

    public static void translateText(Context context, String text, TranslationCallback callback) {
        if (text == null || text.isEmpty()) {
            callback.onSuccess(text);
            return;
        }

        // Если выбран английский язык (EN) — отдаем оригинал сразу
        if ("en".equalsIgnoreCase(LanguageManager.getLanguage(context))) {
            callback.onSuccess(text);
            return;
        }

        // Передаем правильный заголовок авторизации DeepL-Auth-Key
        String authHeader = "DeepL-Auth-Key " + DEEPL_API_KEY;

        DeepLClient.getApi().translateText(authHeader, text, "RU").enqueue(new Callback<DeepLResponse>() {
            @Override
            public void onResponse(Call<DeepLResponse> call, Response<DeepLResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getTranslations() != null &&
                        !response.body().getTranslations().isEmpty()) {

                    String translated = response.body().getTranslations().get(0).getText();
                    callback.onSuccess(translated);
                } else {
                    Log.e("DeepL_API", "Ошибка DeepL HTTP " + response.code());
                    showToast(context, "DeepL ошибка: " + response.code());
                    callback.onError(new Exception("Translation failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<DeepLResponse> call, Throwable t) {
                Log.e("DeepL_API", "Сетевая ошибка DeepL: " + t.getMessage(), t);
                showToast(context, "Ошибка сети DeepL");
                callback.onError(t);
            }
        });
    }

    public static void translateText(String text, TranslationCallback callback) {
        translateText(null, text, callback);
    }

    private static void showToast(Context context, String msg) {
        if (context != null) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
            );
        }
    }
}