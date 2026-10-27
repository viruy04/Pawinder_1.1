package com.example.pawinder_11;

import com.example.pawinder_11.api.DeepLClient;
import com.example.pawinder_11.model.DeepLResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslationManager {

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onError(Throwable t);
    }

    public static void translateText(String text, TranslationCallback callback) {
        if (text == null || text.isEmpty()) {
            callback.onSuccess(text);
            return;
        }

        DeepLClient.getApi().translateText(text, "RU").enqueue(new Callback<DeepLResponse>() {
            @Override
            public void onResponse(Call<DeepLResponse> call, Response<DeepLResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        !response.body().getTranslations().isEmpty()) {
                    String translated = response.body().getTranslations().get(0).getText();
                    callback.onSuccess(translated);
                } else {
                    callback.onError(new Exception("Translation failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<DeepLResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    public static void close() {
        // Больше не нужно закрывать локальные модели
    }
}