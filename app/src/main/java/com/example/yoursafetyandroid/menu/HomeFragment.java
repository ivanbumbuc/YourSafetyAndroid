package com.example.yoursafetyandroid.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.fakeCall.FakeCallActivity;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.location.LocationActivity;
import com.example.yoursafetyandroid.recorder.RecorderActivity;
import com.example.yoursafetyandroid.safetyTimer.SafetyTimerActivity;


public class HomeFragment extends Fragment {

    private ImageView safetyTimer;
    private ImageView limitZone;
    private ImageView location;
    private ImageView recording;
    private ImageView fakeCall;
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
        safetyTimer = (ImageView) rootView.findViewById(R.id.imageViewSafetyTimerButton);
        limitZone = (ImageView) rootView.findViewById(R.id.limitZone);
        location = (ImageView) rootView.findViewById(R.id.locationImageView);
        recording = (ImageView) rootView.findViewById(R.id.RecordingView);
        fakeCall = (ImageView) rootView.findViewById(R.id.FakeCallView);
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
       
    }

}