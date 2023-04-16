package com.example.yoursafetyandroid.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.pushNotification.NotificationService;
import com.example.yoursafetyandroid.safetyTimer.SafetyTimerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


public class HomeFragment extends Fragment {

    private ImageView safetyTimer;
    private ImageView limitZone;
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
        buttonsActions();
        return rootView;
    }

    private void buttonsActions()
    {
        safetyTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sessionIntent = new Intent(getActivity(), SafetyTimerActivity.class);
                sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(sessionIntent);
            }
        });

        limitZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sessionIntent = new Intent(getActivity(), LimitZoneActivity.class);
                sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(sessionIntent);
            }
        });

    }

}