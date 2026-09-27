package com.example.pawinder_11.model;

public class Pet {
    private int id_pet;
    private String api_pet_id;
    private String name;
    private String description;
    private String photo_url;

    // геттеры
    public int getId() { return id_pet; }
    public String getApiPetId() { return api_pet_id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photo_url; }
}
