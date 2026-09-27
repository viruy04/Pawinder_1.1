package com.example.pawinder_11.api;

import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PawinderApi {
    @POST("api/users/login")
    Call<User> login(@Body LoginRequest request);

    @GET("api/pets/type/{typeId}")
    Call<List<Pet>> getPetsByType(@Path("typeId") int typeId);

    @POST("api/swipes")
    Call<Swipe> recordSwipe(@Body SwipeRequest request);

    @POST("api/favorites")
    Call<Favorite> addFavorite(@Body SwipeRequest request);

    @GET("api/favorites/user/{userId}")
    Call<List<Favorite>> getFavorites(@Path("userId") int userId);
}
