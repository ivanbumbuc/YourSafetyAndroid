package com.example.yoursafetyandroid.limitZone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.example.yoursafetyandroid.pushNotification.NotificationType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LimitZoneService extends Service {

    private Handler handler;
    private Runnable runnable;
    private FirebaseFirestore db;
    private String uid;



    @Override
    public void onCreate() {
        super.onCreate();

        db=FirebaseFirestore.getInstance();
        uid = (FirebaseAuth.getInstance().getCurrentUser()).getUid();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                doWork();
                handler.postDelayed(this, 5000); // 2000 milliseconds = 2 seconds
            }
        };

        handler.post(runnable);
    }

    private void doWork() {

        DocumentReference docRef = db.collection("limitZone").document(Objects.requireNonNull(uid));
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && Integer.parseInt(document.getData().get("type").toString()) != LimitType.NONE) {
                    if(Integer.parseInt(document.getData().get("type").toString()) == LimitType.CHILD) {
                        LatLng x1 = new LatLng((double)document.getData().get("latitude"),(double) document.getData().get("longitude"));
                        DocumentReference docRef2 = db.collection("liveLocation").document(Objects.requireNonNull(uid));
                        docRef2.get().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document2 = task2.getResult();
                                        if(document2.exists())
                                        {
                                            LatLng x2 = new LatLng((double)document2.getData().get("latitude"),(double) document2.getData().get("longitude"));
                                            if(calculateDistance(x1,x2) > 100)
                                            {
                                                String parent = document.getData().get("user").toString();
                                                Map<String, Object> info = new HashMap<>();
                                                info.put("typeMessage", 3);
                                                info.put("message", "The child is not in perimeter of limit zone!");
                                                db.collection("liveLocation").document(parent).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(MenuActivity.context, "Alert zone limit!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(MenuActivity.context, "Error alert zone limit!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }
                        });
                    }
                }
            }
        });
    }

    private double calculateDistance(LatLng latLng1, LatLng latLng2) {
        // Create Location objects from LatLng
        Location location1 = new Location("Location 1");
        location1.setLatitude(latLng1.getLatitude());
        location1.setLongitude(latLng1.getLongitude());

        Location location2 = new Location("Location 2");
        location2.setLatitude(latLng2.getLatitude());
        location2.setLongitude(latLng2.getLongitude());

        // Calculate the distance in meters
        return location1.distanceTo(location2);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
