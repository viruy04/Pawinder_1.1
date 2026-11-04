package com.example.pawinder_11.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pawinder_11.model.User;

/** Хранит id вошедшего пользователя на устройстве. */
public class SessionManager {

    private static final String PREFS = "session";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NICKNAME = "nickname";

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void save(Context c, User user) {
        prefs(c).edit()
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_NICKNAME, user.getNickname())
                .apply();
    }

    public static int getUserId(Context c) {
        return prefs(c).getInt(KEY_USER_ID, -1);
    }

    public static String getNickname(Context c) {
        return prefs(c).getString(KEY_NICKNAME, "");
    }

    public static boolean isLoggedIn(Context c) {
        return getUserId(c) != -1;
    }

    public static void logout(Context c) {
        prefs(c).edit().clear().apply();
    }
}
