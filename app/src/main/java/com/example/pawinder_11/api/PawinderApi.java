package com.example.pawinder_11.api;

import com.example.pawinder_11.model.LoginRequest;
import com.example.pawinder_11.model.Pet;
import com.example.pawinder_11.model.RegisterRequest;
import com.example.pawinder_11.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PawinderApi {

    @GET("api/pets")
    Call<List<Pet>> getAllPets();

    @GET("api/pets")
    Call<List<Pet>> getPets(@Query("page") int page, @Query("size") int size);

    @GET("api/pets/type/{typeId}")
    Call<List<Pet>> getPetsByType(@Path("typeId") int typeId);

    @POST("api/auth/login")
    Call<User> login(@Body LoginRequest body);

    @POST("api/auth/register")
    Call<User> register(@Body RegisterRequest body);
}
