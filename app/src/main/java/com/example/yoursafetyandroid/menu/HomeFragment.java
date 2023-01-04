package com.example.yoursafetyandroid.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.login.LoginActivity;
import com.example.yoursafetyandroid.safetyTimer.SafetyTimerActivity;


public class HomeFragment extends Fragment {

    private ImageView safetyTimer;

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

    }

}