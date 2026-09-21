package com.example.pawinder_11;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            openFragment(new FeedFragment());
        }

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragment;

            if (item.getItemId() == R.id.nav_feed) {
                fragment = new FeedFragment();

            } else if (item.getItemId() == R.id.nav_swipe) {
                fragment = new SwipeFragment();

            } else if (item.getItemId() == R.id.nav_favorites) {
                fragment = new FavoritesFragment();

            } else {
                return false;
            }

            openFragment(fragment);
            return true;
        });
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}