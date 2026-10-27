package com.example.pawinder_11.api;

import com.example.pawinder_11.model.DeepLResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DeepLApi {

    @Headers("Authorization: DeepL-Auth-Key bf55676e-d564-46c8-968d-75de95023f1a:fx")
    @FormUrlEncoded
    @POST("v2/translate")
    Call<DeepLResponse> translateText(
            @Field("text") String text,
            @Field("target_lang") String targetLang // например, "RU"
    );
}