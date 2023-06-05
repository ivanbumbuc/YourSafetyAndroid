package com.example.yoursafetyandroid.safetyTimer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.location.LocationService;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.example.yoursafetyandroid.sms.SmsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SafetyTimerActivity extends AppCompatActivity {


    private TextView textViewTime;
    private Button buttonStartStop;

    private ProgressBar progressBarCircle;
    private NumberPicker numberPickerSeconds;
    private NumberPicker numberPickerMinutes;
    private NumberPicker numberPickerHours;

    private static long millisUntilFinished;
    private static long timeTotalInMilliSeconds;
    private static Boolean counterIsActive = false;
    private static Intent serviceIntent;
    private static NotificationChannel channel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety_timer);

        if(Information.sharedPreferences.getString(Information.limitZone,"").equals("on"))
        {
            Intent sessionIntent =new Intent(this, LimitZoneActivity.class);
            // making sure activity stack is cleared before starting landing activity
            //sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(sessionIntent);
        }

        if(channel == null) {
            channel = new NotificationChannel("safetyTime", "SafetyTime", NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getSystemService((NotificationManager.class)).createNotificationChannel(channel);
        }

        viewsSetup();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private final BroadcastReceiver updateTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            millisUntilFinished = intent.getLongExtra(TimerService.TIME_REMAINING, 0);
            textViewTime.setText(timeFormatter(millisUntilFinished));
            progressBarCircle.setProgress((int) (millisUntilFinished / 1000));

            if(millisUntilFinished == 0) {
                stopCountDown();
                if(!Information.sharedPreferences.getString(Information.shareLocation,"").equals("on")) {
                    Intent startServiceIntent = new Intent(MenuActivity.context, LocationService.class);
                    MenuActivity.context.startService(startServiceIntent);
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.shareLocation, "on");
                    editor.commit();
                    editor.apply();
                }
                Toast.makeText(context, "Safety Timer stopped, SOS has been activated!", Toast.LENGTH_LONG).show();
                // Create a handler
                Handler handler = new Handler();
                // Run code after delay
                handler.postDelayed(() -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference docRef = db.collection("locationsPersons").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.getData().get("persons") != null) {
                                List<String> numbers = new ArrayList<>(); // Please add this line if you didn't declare 'numbers' elsewhere
                                List<String> x = (ArrayList<String>)document.getData().get("persons");
                                final AtomicInteger counter = new AtomicInteger(x.size());
                                for(String z: x)
                                {
                                    DocumentReference doc = db.collection("liveLocation").document(z);
                                    doc.get().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            DocumentSnapshot document2 = task2.getResult();
                                            if (document2.exists() && document2.getData().get("accountInformation") != null) {
                                                Map<String, String> person = (Map<String, String>) document2.getData().get("accountInformation");
                                                numbers.add(person.get("phone"));
                                            }
                                        } else {
                                            Log.d("getContact", "get failed with ", task.getException());
                                        }
                                        if (counter.decrementAndGet() == 0) {
                                            SmsService.sendSMSSafetyTimer(numbers);
                                        }
                                    });
                                }
                            }
                        } else {
                            Log.d("getContact", "get failed with ", task.getException());
                        }
                    });
                }, 5000);  // Delay of 5 seconds
            }
        }
    };

    private void viewsSetup() {
        textViewTime = findViewById(R.id.textViewTime);
        buttonStartStop = findViewById(R.id.buttonStartStop);
        progressBarCircle = findViewById(R.id.progressBarCircle);
        numberPickerSeconds = findViewById(R.id.numberPickerSeconds);
        numberPickerMinutes = findViewById(R.id.numberPickerMinutes);
        numberPickerHours = findViewById(R.id.numberPickerHours);

        numberPickerSeconds.setMaxValue(59);
        numberPickerSeconds.setMinValue(0);

        numberPickerMinutes.setMaxValue(59);
        numberPickerMinutes.setMinValue(0);

        numberPickerHours.setMaxValue(23);
        numberPickerHours.setMinValue(0);

        if (counterIsActive) {
            counterActiveSetup();
        }
    }

    @SuppressLint("SetTextI18n")
    private void counterActiveSetup(){
        registerReceiver(updateTime, new IntentFilter(TimerService.COUNTDOWN_UPDATED));
        textViewTime.setText(timeFormatter(millisUntilFinished));
        progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
        buttonStartStop.setText("STOP");

        numberPickerHours.setEnabled(false);
        numberPickerMinutes.setEnabled(false);
        numberPickerSeconds.setEnabled(false);

        progressBarCircle.setMax((int) timeTotalInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeTotalInMilliSeconds / 1000);
    }

    @SuppressLint("DefaultLocale")
    static  String timeFormatter(long milliSeconds) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    private long getCountDownDuration() {
        int hours = numberPickerHours.getValue();
        int minutes = numberPickerMinutes.getValue();
        int seconds = numberPickerSeconds.getValue();

        return (seconds + 60L * minutes + 3600L * hours) * 1000L;
    }

    public void startStop(View view) {
        if (counterIsActive) {
            stopCountDown();
            Toast.makeText(this, "Countdown stopped: " + timeFormatter(millisUntilFinished), Toast.LENGTH_LONG).show();
        } else {
            if(getCountDownDuration() <  30000){
                Toast.makeText(this, "Minimum time is 30 seconds", Toast.LENGTH_LONG).show();
                return;
            }
            startCountDown();
            Toast.makeText(this, "Countdown started: " + timeFormatter(timeTotalInMilliSeconds), Toast.LENGTH_LONG).show();
        }
    }

    private void startCountDown(){
        serviceIntent = new Intent(this, TimerService.class);
        timeTotalInMilliSeconds = getCountDownDuration();
        counterIsActive = true;

        counterActiveSetup();

        serviceIntent.putExtra(TimerService.TIME_REMAINING, timeTotalInMilliSeconds);
        startService(serviceIntent);
    }

    @SuppressLint("SetTextI18n")
    private void stopCountDown(){
        stopService(serviceIntent);
        counterIsActive = false;
        buttonStartStop.setText("START");
        numberPickerHours.setEnabled(true);
        numberPickerMinutes.setEnabled(true);
        numberPickerSeconds.setEnabled(true);
    }

    static void pushNotification(Context context, String title, String content){
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1,
                new Intent(context, SafetyTimerActivity.class).setAction("intentForceClose"), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, "safetyTime")
                .setColor(Color.GRAY)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .build();

        int id = new Random(System.currentTimeMillis()).nextInt(1000);
        NotificationManagerCompat.from(context).notify(id, notification);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}