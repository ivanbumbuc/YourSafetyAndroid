package com.example.yoursafetyandroid.fakeCall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.example.yoursafetyandroid.recorder.RecorderActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FakeCallActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private EditText phoneNumber;
    private FirebaseFirestore db;
    private Switch butonOnOff;
    private TextView trackingOnOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call);

        if(Information.sharedPreferences.getString(Information.limitZone,"").equals("on"))
        {
            Intent sessionIntent =new Intent(this, LimitZoneActivity.class);
            // making sure activity stack is cleared before starting landing activity
           // sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(sessionIntent);
        }
        trackingOnOff = (TextView)findViewById(R.id.textTrackingFakeCall);
        butonOnOff = findViewById(R.id.switchFakeCall);
        phoneNumber = (EditText) findViewById(R.id.editTextFakePhoneNumber);
        db = FirebaseFirestore.getInstance();
        updatePhoneNumber();
        Button change = (Button) findViewById(R.id.buttonFakePhonenNumber);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePhoneNumber();
            }
        });

        Information.sharedPreferences= getSharedPreferences(Information.loginPreference, Context.MODE_PRIVATE);
        if(Information.sharedPreferences.contains(Information.fakeCall) && Information.sharedPreferences.getString(Information.fakeCall,"").equals("on"))
        {
            butonOnOff.setChecked(true);
            trackingOnOff.setText("Fake Call enabled");
            trackingOnOff.setTextColor(Color.GREEN);
            Toast.makeText(this,"Fake Call is enabled", Toast.LENGTH_SHORT).show();
        }
        else
        {
            butonOnOff.setChecked(false);
            trackingOnOff.setText("Fake Call disabled");
            trackingOnOff.setTextColor(Color.RED);
            Toast.makeText(this,"Fake Call is disabled", Toast.LENGTH_SHORT).show();
        }


        butonOnOff.setOnCheckedChangeListener((compoundButton, b) -> {
            if (Settings.canDrawOverlays(this)) {
                if (b) {
                    updateDataBaseFakeCall(true);
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.fakeCall, "on");
                    editor.commit();
                    editor.apply();
                    trackingOnOff.setText("Fake Call enabled");
                    trackingOnOff.setTextColor(Color.GREEN);
                    Toast.makeText(FakeCallActivity.this, "Fake Call is enabled", Toast.LENGTH_SHORT).show();
                    Information.fakeCallValue = phoneNumber.getText().toString();
                } else {
                    updateDataBaseFakeCall(false);
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.fakeCall, "off");
                    editor.commit();
                    editor.apply();
                    trackingOnOff.setText("Fake Call disabled");
                    trackingOnOff.setTextColor(Color.RED);
                    Toast.makeText(FakeCallActivity.this, "Fake Call is disabled", Toast.LENGTH_SHORT).show();
                    Information.fakeCallValue = phoneNumber.getText().toString();
                }
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(intent, 200);
                if (b) {
                    updateDataBaseFakeCall(true);
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.fakeCall, "on");
                    editor.commit();
                    editor.apply();
                    trackingOnOff.setText("Recorder enabled");
                    trackingOnOff.setTextColor(Color.GREEN);
                    Toast.makeText(FakeCallActivity.this, "Fake Call is enabled", Toast.LENGTH_SHORT).show();
                    Information.fakeCallValue = phoneNumber.getText().toString();
                } else {
                    updateDataBaseFakeCall(false);
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.fakeCall, "off");
                    editor.commit();
                    editor.apply();
                    trackingOnOff.setText("Recorder disabled");
                    trackingOnOff.setTextColor(Color.RED);
                    Toast.makeText(FakeCallActivity.this, "Fake Call is disabled", Toast.LENGTH_SHORT).show();
                    Information.fakeCallValue = phoneNumber.getText().toString();
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void updatePhoneNumber()
    {
        DocumentReference docRef = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                System.out.println(document);
                if (document.exists()) {
                    if(document.getData().get("numberFakeCall").toString() != null)
                       phoneNumber.setText(document.getData().get("numberFakeCall").toString());
                }
            }
        });
    }

    private void changePhoneNumber()
    {
        Map<String,Object> info = new HashMap<>();
        info.put("numberFakeCall",phoneNumber.getText().toString());
        db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MenuActivity.context, "Phone number has changed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MenuActivity.context, "Error change phone number!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDataBaseFakeCall(boolean f)
    {
        Map<String,Object> info = new HashMap<>();
        info.put("fakeCall",(boolean)f);
        db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MenuActivity.context, "Error update database!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void simulateCall() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, null); // Replace with your audio file
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}