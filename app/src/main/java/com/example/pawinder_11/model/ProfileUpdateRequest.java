package com.example.pawinder_11.model;

public class ProfileUpdateRequest {
    private String name;
    private String surname;
    private Integer id_city;

    public ProfileUpdateRequest(String name, String surname, Integer idCity) {
        this.name = name;
        this.surname = surname;
        this.id_city = idCity;
    }
}