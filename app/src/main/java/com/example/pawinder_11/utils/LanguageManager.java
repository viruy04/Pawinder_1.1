package com.example.pawinder_11.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class LanguageManager {
    private static final String PREF_NAME = "app_lang_pref";
    private static final String KEY_LANG = "selected_language";

    // Храним выбранный язык в памяти приложения
    private static String currentLanguage = "ru";

    public static String getLanguage(Context context) {
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            currentLanguage = prefs.getString(KEY_LANG, "ru");
        }
        return currentLanguage;
    }

    public static void setLanguage(Context context, String langCode) {
        currentLanguage = langCode;
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_LANG, langCode).apply();
        }
    }
}