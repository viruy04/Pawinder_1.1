package com.example.pawinder_11.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pawinder_11.R;

public class CustomToast {

    public static void showError(Context context, String text) {
        show(context, text, R.drawable.error);
    }

    public static void showInfo(Context context, String text) {
        show(context, text, R.drawable.sms);
    }

    public static void show(Context context, String text, int avatarResId) {
        if (context == null) return;
        try {
            View layout = LayoutInflater.from(context).inflate(R.layout.toast_error_custom, null);
            ImageView ivAvatar = layout.findViewById(R.id.toastAvatar);
            TextView tvMessage = layout.findViewById(R.id.toastMessage);

            if (ivAvatar != null) {
                ivAvatar.setImageResource(avatarResId);
            }
            if (tvMessage != null) {
                tvMessage.setText(text);
            }

            Toast toast = new Toast(context.getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);

            // Приподнимаем тост выше от низа экрана (на 110dp над нижней навигацией)
            int yOffset = (int) (110 * context.getResources().getDisplayMetrics().density);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, yOffset);

            toast.show();
        } catch (Exception e) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }
}