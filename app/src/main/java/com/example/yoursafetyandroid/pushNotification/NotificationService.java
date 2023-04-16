package com.example.yoursafetyandroid.pushNotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationService extends Service {
    private Handler handler;
    private Runnable runnable;
    private FirebaseFirestore db;
    private String uid;

    private static final String CHANNEL_ID = "my_channel_id";
    private static final String CHANNEL_NAME = "My Channel";
    private static final String CHANNEL_DESC = "This is my notification channel";


    @Override
    public void onCreate() {
        super.onCreate();

        db=FirebaseFirestore.getInstance();
        uid = (FirebaseAuth.getInstance().getCurrentUser()).getUid();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Your repeating task here
                doWork();

                handler.postDelayed(this, 5000); // 2000 milliseconds = 2 seconds
            }
        };

        handler.post(runnable);
    }

    private void doWork() {
        DocumentReference docRef = db.collection("liveLocation").document(Objects.requireNonNull(uid));
        System.out.println(docRef);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                System.out.println(document.getData());
                if (document.exists() && Integer.parseInt(document.getData().get("typeMessage").toString()) != NotificationType.NONE) {
                    if(Integer.parseInt(document.getData().get("typeMessage").toString()) == NotificationType.ZONE_LIMIT)
                    {
                        String message = document.getData().get("message").toString();
                        showNotification("Zone Limit",message);
                    }
                    Map<String,Object> info=new HashMap<>();
                    info.put("typeMessage",0);
                    info.put("message","");
                    db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MenuActivity.context, "Notification received!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MenuActivity.context, "Error received notification!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void showNotification(String title, String message) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        long[] vibrationPattern = {0, 500, 250, 500};
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_safety_check_24)
                .setContentTitle(title)
                .setContentText(message)
                .setVibrate(vibrationPattern)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Intent intent = new Intent(this, MenuActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
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