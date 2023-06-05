package com.example.yoursafetyandroid.sms;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SmsService {

    public static String messageToSend="";


    @SuppressLint("MissingPermission")
    public static void sendSMS2(List<String> numereSMS) {
        if (MenuActivity.context == null)
            return;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            for(String phoneNumber : numereSMS) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            messageToSend =
                                    "SOS, I AM IN DANGER AT: " +
                                            "https://www.google.com/maps/search/?api=1&query=" +
                                            document.getData().get("latitude") + "," + document.getData().get("longitude") ;
                            smsManager.sendTextMessage(phoneNumber, null, messageToSend, null, null);
                        }
                    }
                });
            }
            Toast.makeText(MenuActivity.context, "SMS SOS Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MenuActivity.context, "SMS failed to send!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    @SuppressLint("MissingPermission")
    public static void sendSMSSafetyTimer(List<String> numereSMS) {
        if (MenuActivity.context == null)
            return;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            for(String phoneNumber : numereSMS) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference docRef = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            messageToSend =
                                    "Safety Timer, maybe i am in danger at: " +
                                            "https://www.google.com/maps/search/?api=1&query=" +
                                            document.getData().get("latitude") + "," + document.getData().get("longitude") ;
                            smsManager.sendTextMessage(phoneNumber, null, messageToSend, null, null);
                        }
                    }
                });
            }
            Toast.makeText(MenuActivity.context, "SMS SOS Sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(MenuActivity.context, "SMS failed to send!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    @SuppressLint("MissingPermission")
    public static void sendSMS(List<String> numereSMS) {
        if (MenuActivity.context == null)
            return;
        System.out.println("Trimis mesaj");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    messageToSend +=
                            "I AM IN DANGER AT " +
                                    "https://www.google.com/maps/search/?api=1&query=" +
                                     document.getData().get("latitude") + "," + document.getData().get("longitude") ;
                    Intent intent1 = new Intent("locatie");
                    intent1.putExtra("locatie",messageToSend);
                    intent1.putExtra("numereSMS", (Serializable) numereSMS);
                    MenuActivity.context.sendBroadcast(intent1);
                }
            }
        });
    }

    public static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String locatie = intent.getStringExtra("locatie");
            ArrayList<String> numereSMS = intent.getStringArrayListExtra("numereSMS");

            Thread x = new Thread() {
                @Override
                public void run() {
                    try {
                        for (String numar : numereSMS) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(numar, null, locatie, null, null);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            x.start();
        }
    };
}
