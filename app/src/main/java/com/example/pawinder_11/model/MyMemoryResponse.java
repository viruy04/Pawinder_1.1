package com.example.pawinder_11.model;

import com.google.gson.annotations.SerializedName;

public class MyMemoryResponse {

    @SerializedName("responseData")
    private ResponseData responseData;

    @SerializedName("responseStatus")
    private int responseStatus;

    public ResponseData getResponseData() {
        return responseData;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public static class ResponseData {
        @SerializedName("translatedText")
        private String translatedText;

        public String getTranslatedText() {
            return translatedText;
        }
    }
}