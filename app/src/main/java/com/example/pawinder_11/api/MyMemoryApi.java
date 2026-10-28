package com.example.pawinder_11.api;

import com.example.pawinder_11.model.MyMemoryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MyMemoryApi {

    @GET("get")
    Call<MyMemoryResponse> translateText(
            @Query("q") String text,
            @Query(value = "langpair", encoded = true) String langPair,
            @Query("de") String email
    );
}