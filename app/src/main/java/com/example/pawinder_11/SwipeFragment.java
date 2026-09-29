package com.example.pawinder_11;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.Pet;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SwipeFragment extends Fragment {

    public SwipeFragment() {
        super(R.layout.fragment_swipe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ApiClient.getApi().getAllPets().enqueue(new Callback<List<Pet>>() {
            @Override
            public void onResponse(Call<List<Pet>> call, Response<List<Pet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_TEST", "Получено животных: " + response.body().size());
                    for (Pet pet : response.body()) {
                        Log.d("API_TEST", pet.getName() + " — " + pet.getPhotoUrl());
                    }
                    Toast.makeText(getContext(),
                            "Загружено: " + response.body().size(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("API_TEST", "Пустой ответ, код: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Pet>> call, Throwable t) {
                Log.e("API_TEST", "Ошибка сети", t);
            }
        });
    }
}