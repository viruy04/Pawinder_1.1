package com.example.pawinder_11.api;

import com.example.pawinder_11.model.DeepLResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DeepLApi {

    @FormUrlEncoded
    @POST("v2/translate")
    Call<DeepLResponse> translateText(
            @Header("Authorization") String authorizationHeader,
            @Field("text") String text,
            @Field("target_lang") String targetLang
    );
}