package com.example.yoursafetyandroid.fakeCall;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;

public class AcceptCallActivity extends AppCompatActivity {

    private TextView name;
    private ImageButton endCall;
    private Chronometer cronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_call);

        name = findViewById(R.id.textViewAccepCall);
        endCall = findViewById(R.id.imageButtonAcceptCall_end);
        cronometer = findViewById(R.id.chronometer);

        if(Information.fakeCallValue != null)
        {
            name.setText(Information.fakeCallValue);
        }
        else{
            name.setText("0723432879");
        }

        cronometer.start();

        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}