package com.example.yoursafetyandroid.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.FragmentTransitionSupport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;


import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.databinding.ActivityMainBinding;
import com.example.yoursafetyandroid.services.ScreenService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MenuActivity extends AppCompatActivity {

    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HomeFragment homeFragment = new HomeFragment();
        LocationFragment locationFragment = new LocationFragment();
        HistoryFragment historyFragment = new HistoryFragment();
        SettingsFragment settingsFragment = new SettingsFragment();
        context = MenuActivity.this;
        Intent backgroundService = new Intent(this, ScreenService.class);
        startService(backgroundService);
        BottomNavigationView bottomBar = findViewById(R.id.bottomNavigationView);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,homeFragment).commit();


        bottomBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
            {
                case R.id.homeNavbar:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,homeFragment).commit();
                    return true;
                case R.id.locationNavbar:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,locationFragment).commit();
                    return true;
                case R.id.historyNavbar:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,historyFragment).commit();
                    return true;
                case R.id.setingsNavbar:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,settingsFragment).commit();
                    return true;
            }
                return false;
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}