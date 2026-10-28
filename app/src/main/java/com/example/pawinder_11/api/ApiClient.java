package com.example.pawinder_11.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    //нужно будет менять айпишник как я понимаю пока не возведем прилично, это мой домашний лол
    private static final String BASE_URL = "https://pawinder-server.onrender.com/";
    private static Retrofit retrofit;

    public static PawinderApi getApi() {
        if (retrofit == null) {
            // Настройка таймаута ожидания 40 секунд для пробуждения сервера
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(40, TimeUnit.SECONDS)
                    .readTimeout(40, TimeUnit.SECONDS)
                    .writeTimeout(40, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(PawinderApi.class);
    }
}