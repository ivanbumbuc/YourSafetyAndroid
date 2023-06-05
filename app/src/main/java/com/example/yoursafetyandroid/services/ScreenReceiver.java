package com.example.yoursafetyandroid.services;

import static android.content.Context.VIBRATOR_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.location.LocationService;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.example.yoursafetyandroid.recorder.Recorder;
import com.example.yoursafetyandroid.sms.SmsService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
        if(counter == 3) {
            String recorderPermission = Information.sharedPreferences.getString(Information.recorder, "");
            String fakeCallPermission = Information.sharedPreferences.getString(Information.fakeCall, "");
            String sosPermission = Information.sharedPreferences.getString(Information.SOS,"");
            //String locationPermission = Information.sharedPreferences.getString(Information.shareLocation,"");
            if(sosPermission.equals("on"))
            {
                serviceStart();
                if (recorderPermission.equals("on"))
                    recorder();
                if (fakeCallPermission.equals("on"))
                    fakeCall();
            }
            else
                Toast.makeText(context, "You need to activate SOS service!", Toast.LENGTH_LONG).show();
            return;
        }
        timer.cancel();
        timer.start();
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void serviceStart() {
//        List<String> numbers = new ArrayList<>();
//        Toast.makeText(context, "Power Button pressed, SOS has been activated!", Toast.LENGTH_LONG).show();
//        if(!Information.sharedPreferences.getString(Information.shareLocation,"").equals("on"))
//        {
//                Intent startServiceIntent = new Intent(MenuActivity.context, LocationService.class);
//                MenuActivity.context.startService(startServiceIntent);
//        }
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference docRef = db.collection("locationsPersons").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
//        docRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document.exists() && document.getData().get("persons") != null) {
//                    List<String> x = (ArrayList<String>)document.getData().get("persons");
//                    final AtomicInteger counter = new AtomicInteger(x.size());
//                    for(String z: x)
//                    {
//                        DocumentReference doc = db.collection("liveLocation").document(z);
//                        doc.get().addOnCompleteListener(task2 -> {
//                            if (task2.isSuccessful()) {
//                                DocumentSnapshot document2 = task2.getResult();
//                                if (document2.exists() && document2.getData().get("accountInformation") != null) {
//                                    Map<String, String> person = (Map<String, String>) document2.getData().get("accountInformation");
//                                    numbers.add(person.get("phone"));
//                                }
//                            } else {
//                                Log.d("getContact", "get failed with ", task.getException());
//                            }
//                            if (counter.decrementAndGet() == 0) {
//                                SmsService.sendSMS(numbers);
//                                System.out.println(numbers);
//                            }
//                        });
//                    }
//                }
//            } else {
//                Log.d("getContact", "get failed with ", task.getException());
//            }
//        });
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void serviceStart() {
        if(!Information.sharedPreferences.getString(Information.shareLocation,"").equals("on")) {
            Intent startServiceIntent = new Intent(MenuActivity.context, LocationService.class);
            MenuActivity.context.startService(startServiceIntent);
            SharedPreferences.Editor editor = Information.sharedPreferences.edit();
            editor.putString(Information.shareLocation, "on");
            editor.commit();
            editor.apply();
        }
        Toast.makeText(context, "Power Button pressed, SOS has been activated!", Toast.LENGTH_LONG).show();
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
                                    SmsService.sendSMS2(numbers);
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