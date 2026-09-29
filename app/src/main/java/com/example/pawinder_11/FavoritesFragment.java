package com.example.pawinder_11;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FavoritesFragment extends Fragment {

    private ImageView avatar;

    // Доступные аватарки
    private final int[] avatars = {
            R.drawable.image_1,
            R.drawable.image_2,
            R.drawable.image_3,
            R.drawable.image_4,
            R.drawable.image_5,
            R.drawable.image_6,
            R.drawable.image_7,
            R.drawable.image_8,
            R.drawable.image_9
    };

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        avatar = view.findViewById(R.id.ivAvatar);

        // Нажатие на аватарку
        avatar.setOnClickListener(v -> showAvatarChooser());

        // Нажатие на карандаш
        ImageView editAvatar = view.findViewById(R.id.ivEditAvatar);

        editAvatar.setOnClickListener(v -> showAvatarChooser());
    }

    private void showAvatarChooser() {

        GridLayout grid = new GridLayout(requireContext());

        // 3 аватарки в строке
        grid.setColumnCount(3);

        int padding = dpToPx(10);
        grid.setPadding(padding, padding, padding, padding);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Выберите аватар")
                .setView(grid)
                .create();

        for (int avatarResource : avatars) {

            ImageView imageView = new ImageView(requireContext());

            imageView.setImageResource(avatarResource);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams();

            params.width = dpToPx(85);
            params.height = dpToPx(85);

            params.setMargins(
                    dpToPx(5),
                    dpToPx(5),
                    dpToPx(5),
                    dpToPx(5)
            );

            imageView.setLayoutParams(params);

            // Выбор аватарки
            imageView.setOnClickListener(v -> {

                avatar.setImageResource(avatarResource);

                dialog.dismiss();
            });

            grid.addView(imageView);
        }

        dialog.show();
    }

    private int dpToPx(int dp) {

        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}