package com.example.yoursafetyandroid.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationActivity extends AppCompatActivity {

    private  FirebaseFirestore db;
    private Switch buttonOnOffLocation;
    private Switch buttonOnOffHistory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        db = FirebaseFirestore.getInstance();
        buttonOnOffLocation = findViewById(R.id.switchLocation);
        buttonOnOffHistory = findViewById(R.id.switchHistory);
        setButtonsEnable();
        setValueLocation();
        setValueHistory();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void setButtonsEnable()
    {
        DocumentReference docRef2 = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        docRef2.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    buttonOnOffLocation.setChecked((boolean) document.getData().get("shareLocation"));
                    buttonOnOffHistory.setChecked((boolean) document.getData().get("historyLocation"));
                }
            }
        });
    }

    private void setValueLocation()
    {
        buttonOnOffLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String user = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                if(b)
                {
                    boolean isMyServiceRunning = isServiceRunning(LocationService.class);
                    if (!isMyServiceRunning) {
                        Intent startServiceIntent = new Intent(MenuActivity.context, LocationService.class);
                        MenuActivity.context.startService(startServiceIntent);
                    }

                    Map<String,Object> info = new HashMap<>();
                    info.put("shareLocation",true);

                    db.collection("liveLocation").document(user).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MenuActivity.context, "Location set ON!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MenuActivity.context, "Error set location!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Map<String,Object> info = new HashMap<>();
                    info.put("shareLocation",false);
                    boolean isMyServiceRunning = isServiceRunning(LocationService.class);
                    if (isMyServiceRunning) {
                        Intent stopServiceIntent = new Intent(MenuActivity.context, LocationService.class);
                        MenuActivity.context.stopService(stopServiceIntent);
                    }

                    db.collection("liveLocation").document(user).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MenuActivity.context, "Location set OFF!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MenuActivity.context, "Error set location!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void setValueHistory()
    {
        buttonOnOffHistory.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String user = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                if(b)
                {
                    Map<String,Object> info = new HashMap<>();
                    info.put("historyLocation",true);

                    db.collection("liveLocation").document(user).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MenuActivity.context, "Location history set ON!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MenuActivity.context, "Error set location history!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    Map<String,Object> info = new HashMap<>();
                    info.put("historyLocation",false);

                    db.collection("liveLocation").document(user).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MenuActivity.context, "Location history set OFF!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MenuActivity.context, "Error set location history!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}