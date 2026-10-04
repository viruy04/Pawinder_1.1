package com.example.pawinder_11;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.Pet;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SwipeFragment extends Fragment {

    private ProgressBar progressBar;
    private View contentContainer;
    private CardView petCardView;

    private ImageView ivPetPhoto;
    private TextView tvPetName;
    private TextView tvPetDescription;

    private View btnDislike;
    private View btnLike;

    private List<Pet> petList = new ArrayList<>();
    private int currentIndex = 0;

    private String currentPhotoUrl = ""; // Для хранения текущей ссылки на фото

    // Переменные для свайпа
    private float dX, dY;
    private float startX, startY;

    public SwipeFragment() {
        super(R.layout.fragment_swipe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        contentContainer = view.findViewById(R.id.contentContainer);
        petCardView = view.findViewById(R.id.petCardView);

        ivPetPhoto = view.findViewById(R.id.ivPetPhoto);
        tvPetName = view.findViewById(R.id.tvPetName);
        tvPetDescription = view.findViewById(R.id.tvPetDescription);

        btnDislike = view.findViewById(R.id.btnDislike);
        btnLike = view.findViewById(R.id.btnLike);

        setupSwipeAndLongPressGesture();

        btnDislike.setOnClickListener(v -> swipeCard(false));
        btnLike.setOnClickListener(v -> swipeCard(true));

        progressBar.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);

        loadPetsFromApi();
    }

    private void loadPetsFromApi() {
        ApiClient.getApi().getAllPets().enqueue(new Callback<List<Pet>>() {
            @Override
            public void onResponse(Call<List<Pet>> call, Response<List<Pet>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    petList = response.body();
                    currentIndex = 0;

                    preloadAllPetImages();

                    contentContainer.setVisibility(View.VISIBLE);
                    displayPet(petList.get(currentIndex));
                } else {
                    Toast.makeText(getContext(), "Список питомцев пуст", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pet>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("API_ERROR", "Ошибка сети", t);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preloadAllPetImages() {
        if (petList == null || getContext() == null) return;

        for (Pet p : petList) {
            String url = p.getPhotoUrl();
            if (url != null && !url.trim().isEmpty()) {
                if (url.startsWith("/")) {
                    url = "https://pawinderserver-production.up.railway.app" + url;
                }
                Glide.with(requireContext())
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .preload();
            }
        }
    }

    private void displayPet(Pet pet) {
        tvPetName.setText(pet.getName());
        tvPetDescription.setText(pet.getDescription());

        String photoUrl = pet.getPhotoUrl();
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            if (photoUrl.startsWith("/")) {
                photoUrl = "https://pawinderserver-production.up.railway.app" + photoUrl;
            }
            currentPhotoUrl = photoUrl; // Сохраняем ссылку для полного экрана

            Glide.with(requireContext())
                    .load(photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(ivPetPhoto);
        } else {
            currentPhotoUrl = "";
        }

        // Сброс позиции при 0% прозрачности, чтобы исключить скачки
        petCardView.setTranslationX(0);
        petCardView.setTranslationY(0);
        petCardView.setRotation(0);
        petCardView.setAlpha(0f);

        // Плавное появление
        petCardView.animate()
                .alpha(1f)
                .setDuration(150)
                .start();
    }

    // Красивое модальное окно с полупрозрачным фоном, закругленными углами и анимацией
    private void showFullImageDialog(String url) {
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar);

        // Корневой макет с полупрозрачным темным фоном (70% чёрного)
        FrameLayout rootLayout = new FrameLayout(requireContext());
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootLayout.setBackgroundColor(Color.parseColor("#B3000000"));

        // ImageView для фото (чуть шире карточки, без закругленных углов)
        ImageView fullImageView = new ImageView(requireContext());
        int horizontalMargin = dpToPx(12); // Чуть шире карточки (отступы по бокам всего 12dp)
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        imageParams.gravity = Gravity.CENTER;
        imageParams.setMargins(horizontalMargin, 0, horizontalMargin, 0);
        fullImageView.setLayoutParams(imageParams);
        fullImageView.setAdjustViewBounds(true);
        fullImageView.setMaxHeight(dpToPx(600));
        fullImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(requireContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(fullImageView);

        rootLayout.addView(fullImageView);

        dialog.setContentView(rootLayout);

        // Начальное состояние для анимации появления (прозрачность 0, уменьшенный масштаб)
        rootLayout.setAlpha(0f);
        fullImageView.setScaleX(0.9f);
        fullImageView.setScaleY(0.9f);

        // Плавная анимация появления
        rootLayout.animate().alpha(1f).setDuration(200).start();
        fullImageView.animate().scaleX(1f).scaleY(1f).setDuration(200).start();

        // Закрытие по клику с плавной анимацией исчезновения
        View.OnClickListener dismissListener = v -> {
            rootLayout.animate()
                    .alpha(0f)
                    .setDuration(180)
                    .withEndAction(dialog::dismiss)
                    .start();
            fullImageView.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(180)
                    .start();
        };

        rootLayout.setOnClickListener(dismissListener);
        fullImageView.setOnClickListener(dismissListener);

        dialog.show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeAndLongPressGesture() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable longPressRunnable = () -> {
            if (!currentPhotoUrl.isEmpty()) {
                showFullImageDialog(currentPhotoUrl);
            }
        };

        petCardView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    startX = event.getRawX();
                    startY = event.getRawY();

                    // Запускаем таймер зажима на 500мс
                    handler.postDelayed(longPressRunnable, 500);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaXMove = Math.abs(event.getRawX() - startX);
                    float deltaYMove = Math.abs(event.getRawY() - startY);

                    // Если палец сдвинулся больше чем на 15 пикселей — это свайп, отменяем зажим
                    if (deltaXMove > 15 || deltaYMove > 15) {
                        handler.removeCallbacks(longPressRunnable);
                    }

                    float newX = event.getRawX() + dX;
                    v.setTranslationX(newX);
                    v.setRotation(newX / 25f);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Отменяем таймер зажима при отпускании пальца
                    handler.removeCallbacks(longPressRunnable);

                    float deltaX = event.getRawX() - startX;
                    if (Math.abs(deltaX) > 250) {
                        swipeCard(deltaX > 0);
                    } else {
                        v.animate()
                                .translationX(0)
                                .translationY(0)
                                .rotation(0)
                                .setDuration(200)
                                .start();
                    }
                    return true;
            }
            return false;
        });
    }

    private void swipeCard(boolean isRight) {
        float targetX = isRight ? 1200f : -1200f;
        petCardView.animate()
                .translationX(targetX)
                .rotation(isRight ? 30f : -30f)
                .setDuration(250)
                .withEndAction(this::showNextPet)
                .start();
    }

    private void showNextPet() {
        if (petList == null || petList.isEmpty()) return;

        currentIndex++;
        if (currentIndex >= petList.size()) {
            currentIndex = 0;
        }

        displayPet(petList.get(currentIndex));
    }
}