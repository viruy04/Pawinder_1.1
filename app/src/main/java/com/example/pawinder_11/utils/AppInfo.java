package com.example.pawinder_11.utils;

import com.example.pawinder_11.BuildConfig;

public class AppInfo {

    public static String format() {
        return BuildConfig.VERSION_NAME
                + "+"
                + BuildConfig.GIT_SHA
                + (BuildConfig.GIT_DIRTY ? "-dirty" : "")
                + " ("
                + BuildConfig.BUILD_TYPE
                + ")";
    }
}
