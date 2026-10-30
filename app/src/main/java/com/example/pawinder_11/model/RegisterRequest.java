package com.example.pawinder_11.model;

public class RegisterRequest {
    private String nickname;
    private String email;
    private String password;

    public RegisterRequest(String nickname, String email, String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }
}
