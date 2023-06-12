package com.example.yoursafetyandroid.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.yoursafetyandroid.main.MainActivity;
import com.example.yoursafetyandroid.sms.SmsService;

public class ScreenService extends Service {
    private ScreenReceiver screenReceiver = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");

        intentFilter.setPriority(100);

        screenReceiver = new ScreenReceiver(this);

        registerReceiver(screenReceiver, intentFilter);

        registerReceiver(SmsService.receiver, new IntentFilter("locatie"));
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "YourSafety";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setAction("intentForceClose");

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setColor(Color.GRAY)
                .setContentTitle("YourSafety is running in background")
                .setContentText("Click to stop the application")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(resultPendingIntent)
                .build();

        startForeground(2, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(screenReceiver!=null)
        {
            unregisterReceiver(screenReceiver);
            Log.d("SCREEN_TOGGLE_TAG", "Service onDestroy: ScreenReceiver is unregistered.");
        }

    }

}