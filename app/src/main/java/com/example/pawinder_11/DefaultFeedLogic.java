package com.example.pawinder_11;

import java.util.List;

public class DefaultFeedLogic implements FeedLogicContract {

    @Override
    public String resolvePhotoUrl(String photoUrl, String baseUrl) {
        if (photoUrl == null) {
            return null;
        }

        String url = photoUrl.trim();

        if (url.isEmpty()) {
            return null;
        }

        if (url.startsWith("/")) {
            return baseUrl + url;
        }

        return url;
    }

    @Override
    public boolean isUsableResponse(boolean successful, List<?> body) {
        return successful && body != null;
    }

    @Override
    public String networkErrorMessage(Throwable t) {
        String message = (t == null) ? null : t.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return FeedLogic.NETWORK_ERROR_PREFIX;
        }

        return FeedLogic.NETWORK_ERROR_PREFIX + ": " + message;
    }
}