package com.example.pawinder_11;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.Pet;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedFragment extends Fragment {

    private ViewPager2 viewPagerFeed;
    private ProgressBar progressBarFeed;
    private FeedAdapter feedAdapter;
    private final List<Pet> petList = new ArrayList<>();

    public FeedFragment() {
        super(R.layout.fragment_feed);
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
        ApiClient.getApi().getAllPets().enqueue(new Callback<List<Pet>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pet>> call, @NonNull Response<List<Pet>> response) {
                if (!isAdded()) return;
                progressBarFeed.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    petList.clear();
                    petList.addAll(response.body());
                    feedAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Не удалось загрузить ленту", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Pet>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBarFeed.setVisibility(View.GONE);
                Log.e("FeedFragment", "Error loading feed", t);
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}