package com.example.pawinder_11.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeepLClient {
    private static final String BASE_URL = "https://api-free.deepl.com/";
    private static Retrofit retrofit;

    public static DeepLApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(DeepLApi.class);
    }
}