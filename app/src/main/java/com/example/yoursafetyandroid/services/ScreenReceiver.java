package com.example.yoursafetyandroid.services;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.recorder.Recorder;

public class ScreenReceiver extends BroadcastReceiver {

    private final Context context;
    private int counter = 0;
    public static int time = 30;
    public ScreenReceiver(Context context) {
        this.context = context;
    }

    private final CountDownTimer timer = new CountDownTimer(1500, 1000) {
        @Override
        public void onTick(long l) { }

        @Override
        public void onFinish() {
            counter = 0;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void count() {
        counter++;
        if(counter == 5) {
            serviceStart();
            recorder();
            fakeCall();
            return;
        }
        timer.cancel();
        timer.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void serviceStart() {
        Toast.makeText(context, "Power Button pressed, SOS has been activated!", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_SCREEN_OFF.equals(action)) {
            count();
        }
    }

    private void recorder()
    {

        CountDownTimer countDowntimer = new CountDownTimer(time * 1000L, time * 1000L) {
            public void onTick(long millisUntilFinished) {
                Toast.makeText(context, "Recording has started", Toast.LENGTH_LONG).show();
                Recorder.startRecording2();
            }


            public void onFinish() {
                Toast.makeText(context, "Recording has stopped", Toast.LENGTH_LONG).show();
                Recorder.stopRecording();

            }};countDowntimer.start();
    }

    private void fakeCall()
    {
        Vibrator vibrator=(Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(2000);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(Information.phoneFake!=null)
            intent.setData(Uri.parse("tel: "+ Information.phoneFake));
        else
            intent.setData(Uri.parse("tel: 0722222222"));
        context.startActivity(intent);
    }

}