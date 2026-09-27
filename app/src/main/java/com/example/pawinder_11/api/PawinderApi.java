package com.example.pawinder_11.api;

import com.example.pawinder_11.model.Pet;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PawinderApi {

    @GET("api/pets")
    Call<List<Pet>> getAllPets();

    @GET("api/pets/type/{typeId}")
    Call<List<Pet>> getPetsByType(@Path("typeId") int typeId);
}