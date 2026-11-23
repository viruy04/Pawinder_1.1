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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pawinder_11.R;
import com.example.pawinder_11.api.ApiClient;
import com.example.pawinder_11.model.City;
import com.example.pawinder_11.model.Country;
import com.example.pawinder_11.model.Pet;
import com.example.pawinder_11.model.Profile;
import com.example.pawinder_11.model.ProfileUpdateRequest;
import com.example.pawinder_11.ui.auth.LoginActivity;
import com.example.pawinder_11.utils.CustomToast;
import com.example.pawinder_11.utils.SessionManager;
import com.example.pawinder_11.utils.TranslationManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesFragment extends Fragment {

    // ===== ПОДКЛЮЧЕННЫЕ КАРТИНКИ АВАТАРОК ДЛЯ ТОСТОВ =====
    private static final int TOAST_AVATAR_ERROR = R.drawable.error; // Для ошибок
    private static final int TOAST_AVATAR_INFO  = R.drawable.sms;   // Для уведомлений и загрузок

    private ImageView avatar;
    private TextView tvNickname;
    private TextView tvSurname;
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvCountry;
    private TextView tvCity;

    private Profile profile;

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

    private LinearLayout favoritesList;

    public FavoritesFragment() {
        super(R.layout.fragment_favorites);
    }

    public void refreshLanguage() {
        if (isAdded()) {
            loadFavorites();
        }
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        avatar = view.findViewById(R.id.ivAvatar);

        avatar.setOnClickListener(v -> showAvatarChooser());

        ImageView editAvatar = view.findViewById(R.id.ivEditAvatar);
        if (editAvatar != null) {
            editAvatar.setOnClickListener(v -> showAvatarChooser());
        }

        favoritesList = view.findViewById(R.id.favoritesList);
        tvNickname = view.findViewById(R.id.tvNickname);
        tvSurname = view.findViewById(R.id.tvSurname);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvCountry = view.findViewById(R.id.tvCountry);
        tvCity = view.findViewById(R.id.tvCity);

        tvNickname.setText(SessionManager.getNickname(requireContext()));

        showProfileLoadingState();
        loadProfile();

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditDialog());

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            SessionManager.logout(requireContext());
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    // ---------- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ КАСТОМНЫХ ТОСТОВ ----------

    private void showErrorToast(String text) {
        if (getContext() != null) {
            CustomToast.showError(requireContext(), text);
        }
    }

    private void showInfoToast(String text) {
        if (getContext() != null) {
            CustomToast.showInfo(requireContext(), text);
        }
    }

    // ---------- Избранное ----------

    private void loadFavorites() {
        int userId = SessionManager.getUserId(requireContext());
        if (userId == -1) return;

        showFavoritesMessage("Загрузка…");
        ApiClient.getApi().getFavorites(userId).enqueue(new Callback<List<Pet>>() {
            @Override
            public void onResponse(Call<List<Pet>> call, Response<List<Pet>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    renderFavorites(response.body());
                } else {
                    showFavoritesMessage("Не удалось загрузить избранное: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Pet>> call, Throwable t) {
                if (!isAdded()) return;
                showFavoritesMessage("Нет связи с сервером");
            }
        });
    }

    private void renderFavorites(List<Pet> favorites) {
        favoritesList.removeAllViews();
        if (favorites.isEmpty()) {
            showFavoritesMessage("Пока ничего нет. Свайпните вправо, чтобы добавить питомца.");
            return;
        }

        for (Pet pet : favorites) {
            View item = getLayoutInflater().inflate(R.layout.item_favorite_pet, favoritesList, false);
            TextView tvFavName = item.findViewById(R.id.tvFavName);
            ImageView ivFavPhoto = item.findViewById(R.id.ivFavPhoto);
            View btnFavRemove = item.findViewById(R.id.btnFavRemove);

            tvFavName.setText(pet.getName());

            TranslationManager.translateText(requireContext(), pet.getName(), new TranslationManager.TranslationCallback() {
                @Override
                public void onSuccess(String translatedText) {
                    if (isAdded()) {
                        tvFavName.setText(translatedText);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (isAdded()) {
                        tvFavName.setText(pet.getName());
                    }
                }
            });

            String url = pet.getPhotoUrl();
            if (url != null && !url.trim().isEmpty()) {
                if (url.startsWith("/")) {
                    url = "https://pawinder-server.onrender.com" + url;
                }
                Glide.with(this).load(url).centerCrop().into(ivFavPhoto);
            }

            btnFavRemove.setOnClickListener(v -> removeFavorite(pet));
            favoritesList.addView(item);
        }
    }

    private void removeFavorite(Pet pet) {
        int userId = SessionManager.getUserId(requireContext());
        ApiClient.getApi().removeFavorite(userId, pet.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    loadFavorites();
                } else {
                    showErrorToast("Не удалось удалить: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                showErrorToast("Нет связи с сервером");
            }
        });
    }

    private void showFavoritesMessage(String text) {
        favoritesList.removeAllViews();
        TextView message = new TextView(requireContext());
        message.setText(text);
        message.setTextColor(0xFF555555);
        message.setTextSize(16);
        favoritesList.addView(message);
    }

    // ---------- Профиль ----------

    private void showProfileLoadingState() {
        tvName.setText("Загрузка…");
        tvSurname.setText("");
        tvEmail.setText("Загрузка…");
        tvCountry.setText("Загрузка…");
        tvCity.setText("");
    }

    private void showProfileErrorState(String message) {
        tvName.setText(message);
        tvSurname.setText("");
        tvEmail.setText("—");
        tvCountry.setText("—");
        tvCity.setText("");
    }

    private void loadProfile() {
        int userId = SessionManager.getUserId(requireContext());
        if (userId == -1) return;

        showProfileLoadingState();

        ApiClient.getApi().getProfile(userId).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    profile = response.body();
                    bindProfile();
                } else {
                    showProfileErrorState("Не удалось загрузить");
                    showErrorToast("Не удалось загрузить профиль: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                if (!isAdded()) return;
                showProfileErrorState("Нет связи с сервером");
                showErrorToast("Нет связи с сервером");
            }
        });
    }

    private void bindProfile() {
        if (profile == null) return;

        tvNickname.setText(orDefault(profile.getNickname(), SessionManager.getNickname(requireContext())));

        String name = profile.getName();
        String surname = profile.getSurname();
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasSurname = surname != null && !surname.trim().isEmpty();

        if (hasName && hasSurname) {
            tvName.setText(name);
            tvSurname.setText(surname);
        } else if (hasName) {
            tvName.setText(name);
            tvSurname.setText("");
        } else if (hasSurname) {
            tvName.setText(surname);
            tvSurname.setText("");
        } else {
            tvName.setText("Не указано");
            tvSurname.setText("");
        }

        tvEmail.setText(orDefault(profile.getEmail(), "Не указана"));

        String country = profile.getCountry();
        String city = profile.getCity();
        boolean hasCountry = country != null && !country.trim().isEmpty();
        boolean hasCity = city != null && !city.trim().isEmpty();

        if (hasCountry && hasCity) {
            tvCountry.setText(country + ", ");
            tvCity.setText(city);
        } else if (hasCountry) {
            tvCountry.setText(country);
            tvCity.setText("");
        } else if (hasCity) {
            tvCountry.setText(city);
            tvCity.setText("");
        } else {
            tvCountry.setText("Не указано");
            tvCity.setText("");
        }
    }

    private static String orDefault(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }

    // ---------- Редактирование профиля ----------

    private void showEditDialog() {
        if (profile == null) {
            showInfoToast("Профиль ещё загружается");
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
        Integer[] cityToSelect = { profile.getIdCity() };

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
                        showErrorToast("Не удалось загрузить города");
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
                showErrorToast("Не удалось загрузить страны");
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Редактировать профиль")
                .setView(dialogView)
                .setPositiveButton("Сохранить", null)
                .setNegativeButton("Отмена", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int countryPos = spCountry.getSelectedItemPosition();
            int cityPos = spCity.getSelectedItemPosition();

            if (countryPos > 0 && (cityPos <= 0 || cityPos > cities.size())) {
                showErrorToast("Пожалуйста, выберите город");
                return;
            }

            Integer idCity = (cityPos > 0 && cityPos <= cities.size())
                    ? cities.get(cityPos - 1).getId()
                    : null;

            saveProfile(etName.getText().toString(),
                    etSurname.getText().toString(), idCity);

            dialog.dismiss();
        });
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
                } else if (response.code() == 400) {
                    showErrorToast("Проверьте введённые данные");
                } else {
                    showErrorToast("Ошибка сервера: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                if (!isAdded()) return;
                showErrorToast("Нет связи с сервером");
            }
        });
    }

    // ---------- Аватар ----------

    private void showAvatarChooser() {
        GridLayout grid = new GridLayout(requireContext());
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

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(85);
            params.height = dpToPx(85);
            params.setMargins(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
            imageView.setLayoutParams(params);

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