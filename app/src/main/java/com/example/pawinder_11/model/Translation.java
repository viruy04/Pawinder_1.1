package com.example.pawinder_11.model;

import com.google.gson.annotations.SerializedName;

public class Translation {
    @SerializedName("detected_source_language")
    private String detectedSourceLanguage;

    @SerializedName("text")
    private String text;

    public String getText() {
        return text;
    }
}