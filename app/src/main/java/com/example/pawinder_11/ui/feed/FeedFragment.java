package com.example.pawinder_11.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pawinder_11.logic.feed.FeedLogic;
import com.example.pawinder_11.logic.feed.FeedLogicProvider;
import com.example.pawinder_11.R;
import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.Pet;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedFragment extends Fragment {

    private ViewPager2 viewPagerFeed;
    private View progressBarFeed;
    private FeedAdapter feedAdapter;
    private final List<Pet> petList = new ArrayList<>();

    public FeedFragment() {
        super(R.layout.fragment_feed);
    }

    // Обновление языка для текущей ленты без сброса скролла
    public void refreshLanguage() {
        if (feedAdapter != null) {
            feedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerFeed = view.findViewById(R.id.viewPagerFeed);
        progressBarFeed = view.findViewById(R.id.progressBarFeed);

        // Вертикальные свайпы (как в Reels / TikTok)
        viewPagerFeed.setOrientation(ViewPager2.ORIENTATION_VERTICAL);

        feedAdapter = new FeedAdapter(requireContext(), petList);
        viewPagerFeed.setAdapter(feedAdapter);

        loadPets();
    }


    private void loadPets() {
        progressBarFeed.setVisibility(View.VISIBLE);

        // 1. Проверка внедренных отказов (Fault Injection)
        int faultScenario = FeedFaultInjector.getScenario(requireActivity());

        if (faultScenario == FeedFaultInjector.HTTP_500) {
            handleLoadError("Не удалось загрузить ленту (HTTP 500)");
            return;
        }

        if (faultScenario == FeedFaultInjector.NETWORK_ERROR) {
            handleLoadError("Ошибка сети: тестовый сетевой сбой");
            return;
        }

        // 2. Реальный запрос к API
        ApiClient.getApi().getAllPets().enqueue(new Callback<List<Pet>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<Pet>> call,
                    @NonNull Response<List<Pet>> response
            ) {
                if (!isAdded()) return;

                progressBarFeed.setVisibility(View.GONE);

                // 3. Использование единого безопасного метода обновления
                // Мы НЕ делаем petList.clear() и petList.addAll() здесь вручную!
                // Всё это делает replaceItemsSafely внутри FeedLogic

                if (response.isSuccessful() && response.body() != null) {
                    // Пытаемся безопасно обновить список
                    boolean updated = FeedLogic.replaceItemsSafely(petList, response.body());

                    if (updated) {
                        feedAdapter.notifyDataSetChanged();
                    } else {
                        // Если логика вернула false (например, данные пустые или некорректные)
                        Toast.makeText(
                                requireContext(),
                                "Не удалось обработать данные ленты",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                } else {
                    // Если сервер вернул ошибку (4xx, 5xx) или пустое тело
                    Toast.makeText(
                            requireContext(),
                            "Ошибка сервера: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<Pet>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;

                Log.e("FeedFragment", "Network error", t);
                // При сетевой ошибке мы НЕ трогаем petList, старые карточки остаются
                handleLoadError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void handleLoadError(String message) {
        if (!isAdded()) return;

        progressBarFeed.setVisibility(View.GONE);

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}