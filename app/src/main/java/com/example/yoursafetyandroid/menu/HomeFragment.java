package com.example.yoursafetyandroid.menu;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.fakeCall.FakeCallActivity;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.location.LocationActivity;
import com.example.yoursafetyandroid.location.LocationService;
import com.example.yoursafetyandroid.recorder.RecorderActivity;
import com.example.yoursafetyandroid.safetyTimer.SafetyTimerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class HomeFragment extends Fragment {

    private ImageView safetyTimer;
    private ImageView limitZone;
    private ImageView location;
    private ImageView recording;
    private ImageView fakeCall;
    private ImageView sos;
    private Switch sosButtonActivated;
    private static final int PERMISSION_SEND_SMS = 123;
    public HomeFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        sos = (ImageView) rootView.findViewById(R.id.imageViewSafetyTimerButton);
        limitZone = (ImageView) rootView.findViewById(R.id.limitZone);
        location = (ImageView) rootView.findViewById(R.id.locationImageView);
        recording = (ImageView) rootView.findViewById(R.id.RecordingView);
        fakeCall = (ImageView) rootView.findViewById(R.id.FakeCallView);
        safetyTimer = (ImageView) rootView.findViewById(R.id.imageViewButton);
        sosButtonActivated = rootView.findViewById(R.id.switchSOS);
        if(Information.sharedPreferences.getString(Information.SOS,"").equals("on"))
        {
            sosButtonActivated.setChecked(true);
        }
        else
        {
            sosButtonActivated.setChecked(false);
        }
        buttonsActions();
        return rootView;
    }

    private void buttonsActions()
    {
        safetyTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sessionIntent = new Intent(getActivity(), SafetyTimerActivity.class);
                //sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(sessionIntent);
            }
        });

        limitZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sessionIntent = new Intent(getActivity(), LimitZoneActivity.class);
                //sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(sessionIntent);
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent location = new Intent(getActivity(), LocationActivity.class);
                startActivity(location);
            }
        });

        recording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recorder = new Intent(getActivity(), RecorderActivity.class);
                startActivity(recorder);
            }
        });

        fakeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fakeCall = new Intent(getActivity(), FakeCallActivity.class);
                startActivity(fakeCall);
            }
        });

        sosButtonActivated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.SOS, "on");
                    editor.apply();
                    requestSmsPermission();
                }
                else
                {
                    SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                    editor.putString(Information.SOS, "off");
                    editor.apply();
                }
            }
        });
       
    }

    private void requestSmsPermission() {

        // check permission is given
        if (ContextCompat.checkSelfPermission(MenuActivity.context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(MenuActivity.activity,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
        }
    }





}