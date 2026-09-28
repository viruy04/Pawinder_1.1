package com.example.pawinder_11.model;

public class SwipeRequest {
    private int userId;
    private int petId;
    private boolean like;

    public SwipeRequest(int userId, int petId, boolean like) {
        this.userId = userId;
        this.petId = petId;
        this.like = like;
    }
}