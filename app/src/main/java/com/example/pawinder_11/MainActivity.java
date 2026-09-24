package com.example.pawinder_11;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    // 0 — Лента
    // 1 — Свайпы
    // 2 — Избранное
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReglamentReader.showReglament(this);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // При запуске открываем Ленту
        openFragment(new FeedFragment());
        bottomNavigation.setSelectedItemId(R.id.nav_feed);

        // Нижняя навигация
        bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_feed) {

                currentPage = 0;
                openFragment(new FeedFragment());

            } else if (item.getItemId() == R.id.nav_swipe) {

                currentPage = 1;
                openFragment(new SwipeFragment());

            } else if (item.getItemId() == R.id.nav_favorites) {

                currentPage = 2;
                openFragment(new FavoritesFragment());

            } else {
                return false;
            }

            return true;
        });

        // Кнопка "Назад"
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (currentPage == 2) {

                            // Избранное → Свайпы
                            currentPage = 1;

                            bottomNavigation.setSelectedItemId(
                                    R.id.nav_swipe
                            );

                        } else if (currentPage == 1) {

                            // Свайпы → Лента
                            currentPage = 0;

                            bottomNavigation.setSelectedItemId(
                                    R.id.nav_feed
                            );

                        } else {

                            // Лента → выход
                            finish();
                        }
                    }
                });
    }

    private void openFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}