package com.example.pawinder_11.ui.favorites;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pawinder_11.R;
import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.City;
import com.example.pawinder_11.model.Country;
import com.example.pawinder_11.model.Profile;
import com.example.pawinder_11.model.ProfileUpdateRequest;
import com.example.pawinder_11.ui.auth.LoginActivity;
import com.example.pawinder_11.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    private ImageView avatar;
    private TextView tvNickname;
    private TextView tvSurname;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvCountry;
    private TextView tvCity;

    // последний загруженный с сервера профиль
    private Profile profile;

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

        tvNickname = view.findViewById(R.id.tvNickname);
        tvSurname = view.findViewById(R.id.tvSurname);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvCountry = view.findViewById(R.id.tvCountry);
        tvCity = view.findViewById(R.id.tvCity);

        // ник показываем сразу из сессии, остальное подтянется с сервера
        tvNickname.setText(SessionManager.getNickname(requireContext()));
        loadProfile();

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditDialog());

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            SessionManager.logout(requireContext());
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // ---------- профиль ----------

    private void loadProfile() {
        int userId = SessionManager.getUserId(requireContext());
        ApiClient.getApi().getProfile(userId).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    profile = response.body();
                    bindProfile();
                } else {
                    toast("Не удалось загрузить профиль: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                if (!isAdded()) return;
                toast("Нет связи с сервером");
            }
        });
    }

    private void bindProfile() {
        tvNickname.setText(profile.getNickname());
        tvSurname.setText(orDefault(profile.getSurname(), "Фамилия не указана"));
        tvName.setText(orDefault(profile.getName(), "Имя не указано"));
        tvEmail.setText(profile.getEmail());
        tvCountry.setText(profile.getCountry() != null
                ? profile.getCountry() + ","
                : "Страна не указана");
        tvCity.setText(orDefault(profile.getCity(), "Город не указан"));
    }

    private static String orDefault(String value, String fallback) {
        return (value == null || value.isEmpty()) ? fallback : value;
    }

    // ---------- редактирование ----------

    private void showEditDialog() {
        if (profile == null) {
            toast("Профиль ещё загружается");
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etSurname = dialogView.findViewById(R.id.etSurname);
        Spinner spCountry = dialogView.findViewById(R.id.spCountry);
        Spinner spCity = dialogView.findViewById(R.id.spCity);

        etName.setText(profile.getName());
        etSurname.setText(profile.getSurname());

        List<Country> countries = new ArrayList<>();
        List<City> cities = new ArrayList<>();
        // город, который выбрать после первой загрузки списка городов
        Integer[] cityToSelect = { profile.getIdCity() };

        // слушатель ставим до адаптера, чтобы он сработал и на первом выборе
        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                cities.clear();
                if (position == 0) {
                    bindCities(spCity, cities, null);
                    return;
                }
                int countryId = countries.get(position - 1).getId();
                ApiClient.getApi().getCities(countryId).enqueue(new Callback<List<City>>() {
                    @Override
                    public void onResponse(Call<List<City>> call, Response<List<City>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            cities.clear();
                            cities.addAll(response.body());
                            bindCities(spCity, cities, cityToSelect[0]);
                            cityToSelect[0] = null;
                        }
                    }

                    @Override
                    public void onFailure(Call<List<City>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Не удалось загрузить города");
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ApiClient.getApi().getCountries().enqueue(new Callback<List<Country>>() {
            @Override
            public void onResponse(Call<List<Country>> call, Response<List<Country>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    countries.addAll(response.body());
                    List<String> names = new ArrayList<>();
                    names.add("— страна не выбрана —");
                    int selection = 0;
                    for (int i = 0; i < countries.size(); i++) {
                        names.add(countries.get(i).getCountry());
                        if (profile.getIdCountry() != null
                                && countries.get(i).getId() == profile.getIdCountry()) {
                            selection = i + 1;
                        }
                    }
                    spCountry.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_dropdown_item, names));
                    spCountry.setSelection(selection);
                }
            }

            @Override
            public void onFailure(Call<List<Country>> call, Throwable t) {
                if (!isAdded()) return;
                toast("Не удалось загрузить страны");
            }
        });

        new AlertDialog.Builder(requireContext())
                .setTitle("Редактировать профиль")
                .setView(dialogView)
                .setPositiveButton("Сохранить", (d, w) -> {
                    int cityPos = spCity.getSelectedItemPosition();
                    Integer idCity = (cityPos > 0 && cityPos <= cities.size())
                            ? cities.get(cityPos - 1).getId()
                            : null;
                    saveProfile(etName.getText().toString(),
                            etSurname.getText().toString(), idCity);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void bindCities(Spinner spinner, List<City> cities, Integer selectedId) {
        List<String> names = new ArrayList<>();
        names.add("— город не выбран —");
        int selection = 0;
        for (int i = 0; i < cities.size(); i++) {
            names.add(cities.get(i).getCity());
            if (selectedId != null && cities.get(i).getId() == selectedId) {
                selection = i + 1;
            }
        }
        spinner.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, names));
        spinner.setSelection(selection);
    }

    private void saveProfile(String name, String surname, Integer idCity) {
        int userId = SessionManager.getUserId(requireContext());
        ProfileUpdateRequest body = new ProfileUpdateRequest(name.trim(), surname.trim(), idCity);
        ApiClient.getApi().updateProfile(userId, body).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    profile = response.body();
                    bindProfile();
                    toast("Профиль сохранён");
                } else if (response.code() == 400) {
                    toast("Проверьте введённые данные");
                } else {
                    toast("Ошибка сервера: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                if (!isAdded()) return;
                toast("Нет связи с сервером");
            }
        });
    }

    private void toast(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }

    // ---------- аватар ----------

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