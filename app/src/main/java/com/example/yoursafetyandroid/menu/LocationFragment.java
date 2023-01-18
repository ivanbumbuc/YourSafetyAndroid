package com.example.yoursafetyandroid.menu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.yoursafetyandroid.location.TimeLocation;


public class LocationFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( ActivityCompat.checkSelfPermission(MenuActivity.context, Manifest.permission.ACCESS_BACKGROUND_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            TimeLocation time = new TimeLocation(MenuActivity.context);
            time.cancelTime();
            time.setTime();
        }
        }
    }
