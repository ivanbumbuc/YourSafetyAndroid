package com.example.yoursafetyandroid.main;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private Button login;
    private Button createAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Information.sharedPreferences = getSharedPreferences(Information.yourSafetyLogin, Context.MODE_PRIVATE);
        login = findViewById(R.id.buttonLoginMain);
        createAccount = findViewById(R.id.buttoncreateAccounMain);
        login.setOnClickListener(r -> {
            Intent sessionIntent = new Intent(this, LoginActivity.class);
             sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
             startActivity(sessionIntent);
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}