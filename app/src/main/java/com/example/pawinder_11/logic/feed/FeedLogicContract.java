package com.example.pawinder_11.logic.feed;

import java.util.List;

public interface FeedLogicContract {
    String resolvePhotoUrl(String photoUrl, String baseUrl);

    boolean isUsableResponse(boolean successful, List<?> body);

    String networkErrorMessage(Throwable t);
}