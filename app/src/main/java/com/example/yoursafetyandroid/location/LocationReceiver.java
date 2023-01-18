package com.example.yoursafetyandroid.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.yoursafetyandroid.menu.MenuActivity;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(MenuActivity.context!=null) {
            Location x = new Location();
        }
    }
}