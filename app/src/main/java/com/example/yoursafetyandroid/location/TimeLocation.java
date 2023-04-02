package com.example.yoursafetyandroid.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class TimeLocation {
    private Context context;

    public TimeLocation(Context context)
    {
        this.context=context;
    }

    public void setTime() {
        Intent intent = new Intent(context, LocationReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            long startImmediately = System.currentTimeMillis();
            long repeatEveryFourSeconds = 4 * 1000;

            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, startImmediately, repeatEveryFourSeconds, sender);
        }
    }
    public void cancelTime()
    {
        Intent intent=new Intent(context, LocationReceiver.class);
        PendingIntent sender= PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(am!=null)
        {
            am.cancel(sender);
        }
    }
}
