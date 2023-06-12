package com.example.yoursafetyandroid.fakeCall;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.menu.MenuActivity;

public class IncomingCallActivity extends AppCompatActivity {
    private TextView name;
    private ImageButton accept;
    private ImageButton declin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        name = findViewById(R.id.tv_caller_name);
        accept = findViewById(R.id.imageButtoncall1);
        declin = findViewById(R.id.imageButtoncall2);
        if(Information.fakeCallValue != null)
        {
            name.setText(Information.fakeCallValue);
        }
        else{
            name.setText("0723432879");
        }


        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sessionIntent = new Intent(MenuActivity.activity, AcceptCallActivity.class);
                // making sure activity stack is cleared before starting landing activity
                sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(sessionIntent);
            }
        });

        declin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}