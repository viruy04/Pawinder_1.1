package com.example.pawinder_11.ui.feed;

import android.app.Activity;

public final class FeedFaultInjector {

    public static final int NONE = 0;
    public static final int HTTP_500 = 1;
    public static final int NETWORK_ERROR = 2;

    private FeedFaultInjector() {
    }

    public static int getScenario(Activity activity) {
        if (activity.getIntent().getBooleanExtra("pz04_fault_http500", false)) {
            return HTTP_500;
        }

        if (activity.getIntent().getBooleanExtra("pz04_fault_network", false)) {
            return NETWORK_ERROR;
        }

        return NONE;
    }
}