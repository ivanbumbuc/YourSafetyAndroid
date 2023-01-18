package com.example.yoursafetyandroid.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.firebase.firestore.FirebaseFirestore;

public class ScreenReceiver extends BroadcastReceiver {

    private final Context context;
    private int counter = 0;
    private FirebaseFirestore dbSOSMENU;
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
            return;
        }
        timer.cancel();
        timer.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void serviceStart() {
        //addSOSCallToDatabase();
        Toast.makeText(context, "Power Button pressed 5 times", Toast.LENGTH_LONG).show();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference docRef = db.collection("contacts").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
//        docRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document.exists() && document.getData().get("smsContacts") != null) {
//                    SMSService.sendSMS((ArrayList) Objects.requireNonNull(document.getData().get("smsContacts")));
//                }
//            } else {
//                Log.d("getContact", "get failed with ", task.getException());
//            }
//        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_SCREEN_OFF.equals(action)) {
            count();
        }
    }
}