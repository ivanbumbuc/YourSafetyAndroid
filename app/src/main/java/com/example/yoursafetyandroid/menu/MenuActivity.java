package com.example.yoursafetyandroid.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.FragmentTransitionSupport;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;


import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.pushNotification.NotificationService;
import com.example.yoursafetyandroid.services.ScreenService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.util.Calendar;

public class MenuActivity extends AppCompatActivity {

    public static Context context;
    public static Activity activity;
    public static String pathForRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Information.sharedPreferences = getSharedPreferences(Information.yourSafetyLogin, Context.MODE_PRIVATE);

        if(Information.sharedPreferences.getString(Information.limitZone,"").equals("on"))
        {
            Intent sessionIntent =new Intent(this, LimitZoneActivity.class);
            // making sure activity stack is cleared before starting landing activity
            //sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(sessionIntent);
        }
        HomeFragment homeFragment = new HomeFragment();
        LocationFragment locationFragment = new LocationFragment();
        HistoryFragment historyFragment = new HistoryFragment();
        SettingsFragment settingsFragment = new SettingsFragment();
        context = MenuActivity.this;
        activity = MenuActivity.this;
        Intent backgroundService = new Intent(this, ScreenService.class);
        startService(backgroundService);
        BottomNavigationView bottomBar = findViewById(R.id.bottomNavigationView);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,homeFragment).commit();

        pathForRecording = getFilePathRecording();
        initShareInfromation();

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

        boolean isMyServiceRunning = isServiceRunning(NotificationService.class);
        if (!isMyServiceRunning) {
            Intent startServiceIntent = new Intent(this, NotificationService.class);
            startService(startServiceIntent);
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static String getFilePathRecording()
    {
        ContextWrapper contextWrapper=new ContextWrapper(context.getApplicationContext());
        File musicD=contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file=new File(musicD,"recording_" + Calendar.getInstance().getTime() +".mp3");
        return file.getPath();
    }

    private void initShareInfromation()
    {
        Information.getShareLocationValue = Information.sharedPreferences.getString(Information.shareLocation, "");
    }
}