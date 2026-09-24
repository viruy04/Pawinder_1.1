package com.example.pawinder_11;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReglamentReader {

    public static void showReglament(Context context) {

        try {
            InputStream inputStream =
                    context.getAssets().open("REGLAMENT.txt");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream));

            Log.i("REGLAMENT", "========================================");
            Log.i("REGLAMENT", "        РЕГЛАМЕНТ РАБОТЫ КОМАНДЫ");
            Log.i("REGLAMENT", "========================================");

            String line;

            while ((line = reader.readLine()) != null) {
                Log.i("REGLAMENT", line);
            }

            Log.i("REGLAMENT", "========================================");

            reader.close();

        } catch (Exception e) {

            Log.e(
                    "REGLAMENT",
                    "Файл регламента не найден",
                    e
            );
        }
    }
}