package com.example.pawinder_11.logic.feed;

public final class FeedLogicProvider {

    private static final FeedLogicContract INSTANCE = new DefaultFeedLogic();

    private FeedLogicProvider() {
    }

    public static FeedLogicContract getInstance() {
        return INSTANCE;
    }
}