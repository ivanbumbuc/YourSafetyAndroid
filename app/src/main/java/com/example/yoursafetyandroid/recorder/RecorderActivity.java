package com.example.yoursafetyandroid.recorder;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.services.ScreenReceiver;

public class RecorderActivity extends AppCompatActivity {
    private Switch butonOnOff;
    private TextView trackingOnOff;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        if(Information.sharedPreferences.getString(Information.limitZone,"").equals("on"))
        {
            Intent sessionIntent =new Intent(this, LimitZoneActivity.class);
            // making sure activity stack is cleared before starting landing activity
            //sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(sessionIntent);
        }

        butonOnOff = findViewById(R.id.switchRecorder);

        trackingOnOff = findViewById(R.id.textTracking);
        spinner = findViewById(R.id.spinner1);
        String [] items = {"30 sec", "60 sec", "90 sec", "120 sec"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);


        Information.sharedPreferences= getSharedPreferences(Information.loginPreference, Context.MODE_PRIVATE);
        if(Information.sharedPreferences.contains(Information.recorder) && Information.sharedPreferences.getString(Information.recorder,"").equals("on"))
        {
            butonOnOff.setChecked(true);
            trackingOnOff.setText("Recording enabled");
            trackingOnOff.setTextColor(Color.GREEN);
            Toast.makeText(this,"Recording is enabled", Toast.LENGTH_SHORT).show();
        }
        else
        {
            butonOnOff.setChecked(false);
            trackingOnOff.setText("Recording disabled");
            trackingOnOff.setTextColor(Color.RED);
            Toast.makeText(this,"Recording is disabled", Toast.LENGTH_SHORT).show();
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int x = 0;
                switch (i){
                    case 0:
                        x = 30;
                        break;
                    case 1:
                        x = 60;
                        break;
                    case 2:
                        x = 80;
                        break;
                    case 3:
                        x = 120;
                        break;
                }
                ScreenReceiver.time = x;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        butonOnOff.setOnCheckedChangeListener((compoundButton, b) -> {
            if (ActivityCompat.checkSelfPermission(RecorderActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (b) {
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.recorder, "on");
                    editor.commit();
                    trackingOnOff.setText("Recording enabled");
                    trackingOnOff.setTextColor(Color.GREEN);
                    Toast.makeText(RecorderActivity.this, "Recording is enabled", Toast.LENGTH_SHORT).show();
                    Information.recorder2 = Information.sharedPreferences.getString(Information.recorder, "");
                } else {
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.recorder, "off");
                    editor.commit();
                    trackingOnOff.setText("Recording disabled");
                    trackingOnOff.setTextColor(Color.RED);
                    Toast.makeText(RecorderActivity.this, "Recording is disabled", Toast.LENGTH_SHORT).show();
                    Information.recorder2 = Information.sharedPreferences.getString(Information.recorder, "");
                }
            } else {
                ActivityCompat.requestPermissions(RecorderActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 200);
                if (b) {
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.recorder, "on");
                    editor.commit();
                    trackingOnOff.setText("Recording enabled");
                    trackingOnOff.setTextColor(Color.GREEN);
                    Toast.makeText(RecorderActivity.this, "Recording is enabled", Toast.LENGTH_SHORT).show();
                    Information.recorder2 = Information.sharedPreferences.getString(Information.recorder, "");
                } else {
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.recorder, "off");
                    editor.commit();
                    trackingOnOff.setText("Recording disabled");
                    trackingOnOff.setTextColor(Color.RED);
                    Toast.makeText(RecorderActivity.this, "Recording is disabled", Toast.LENGTH_SHORT).show();
                    Information.recorder2 = Information.sharedPreferences.getString(Information.recorder, "");
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}