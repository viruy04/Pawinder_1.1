package com.example.pawinder_11.utils;

import android.content.Context;
import android.util.Log;

import com.example.pawinder_11.api.MyMemoryClient;
import com.example.pawinder_11.model.MyMemoryResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslationManager {

    private static final String APP_EMAIL = "pawinder.app@gmail.com";

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(Throwable t);
    }

    public static void translateText(Context context, String text, TranslationCallback callback) {
        if (text == null || text.trim().isEmpty()) {
            callback.onSuccess(text);
            return;
        }

        String currentLang = LanguageManager.getLanguage(context); // "ru" или "en"

        // Если выходим на английский интерфейс — переводим с RU на EN
        // Если выходим на русский интерфейс — переводим с EN на RU
        String langPair;
        if ("en".equalsIgnoreCase(currentLang)) {
            langPair = "ru|en";
        } else {
            langPair = "en|ru";
        }

        MyMemoryClient.getApi().translateText(text, langPair, APP_EMAIL).enqueue(new Callback<MyMemoryResponse>() {
            @Override
            public void onResponse(Call<MyMemoryResponse> call, Response<MyMemoryResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getResponseData() != null) {

                    String translated = response.body().getResponseData().getTranslatedText();
                    if (translated != null && !translated.trim().isEmpty()) {
                        callback.onSuccess(translated);
                    } else {
                        callback.onSuccess(text);
                    }
                } else {
                    Log.e("MyMemory_API", "Ошибка MyMemory HTTP " + response.code());
                    callback.onSuccess(text);
                }
            }

            @Override
            public void onFailure(Call<MyMemoryResponse> call, Throwable t) {
                Log.e("MyMemory_API", "Сетевая ошибка MyMemory: " + t.getMessage(), t);
                callback.onSuccess(text);
            }
        });
    }

    public static void translateText(String text, TranslationCallback callback) {
        translateText(null, text, callback);
    }
}