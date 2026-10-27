package com.example.pawinder_11.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeepLResponse {
    @SerializedName("translations")
    private List<Translation> translations;

    public List<Translation> getTranslations() {
        return translations;
    }
}