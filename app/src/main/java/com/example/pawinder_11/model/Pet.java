package com.example.pawinder_11.model;

import com.google.gson.annotations.SerializedName;

public class Pet {
    @SerializedName(value = "id", alternate = {"id_pet"})
    private int id;

    @SerializedName(value = "apiPetId", alternate = {"api_pet_id"})
    private String apiPetId;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName(value = "photoUrl", alternate = {"photo_url"})
    private String photoUrl;

    // Геттеры
    public int getId() {
        return id;
    }

    public String getApiPetId() {
        return apiPetId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}