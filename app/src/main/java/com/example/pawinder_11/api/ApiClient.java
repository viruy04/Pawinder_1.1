package com.example.pawinder_11.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    //нужно будет менять айпишник как я понимаю пока не возведем прилично, это мой домашний лол
    private static final String BASE_URL = "https://pawinderserver-production.up.railway.app/";
    private static Retrofit retrofit;

    public static PawinderApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(PawinderApi.class);
    }
}