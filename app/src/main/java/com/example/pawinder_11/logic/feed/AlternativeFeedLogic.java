package com.example.pawinder_11.logic.feed;

import java.util.List;

public class AlternativeFeedLogic implements FeedLogicContract {

    @Override
    public String resolvePhotoUrl(String photoUrl, String baseUrl) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            return null;
        }

        String url = photoUrl.trim();
        return url.startsWith("/") ? baseUrl + url : url;
    }

    @Override
    public boolean isUsableResponse(boolean successful, List<?> body) {
        if (!successful || body == null) {
            return false;
        }

        return true;
    }

    @Override
    public String networkErrorMessage(Throwable t) {
        if (t == null || t.getMessage() == null
                || t.getMessage().trim().isEmpty()) {
            return FeedLogic.NETWORK_ERROR_PREFIX;
        }

        return FeedLogic.NETWORK_ERROR_PREFIX + ": " + t.getMessage();
    }
}