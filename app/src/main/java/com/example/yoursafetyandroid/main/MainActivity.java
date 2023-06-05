package com.example.yoursafetyandroid.main;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.limitZone.LimitZoneActivity;
import com.example.yoursafetyandroid.login.CreateAccountActivity;
import com.example.yoursafetyandroid.login.LoginActivity;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.example.yoursafetyandroid.pushNotification.NotificationService;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private Button login;
    private Button createAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Information.sharedPreferences = getSharedPreferences(Information.yourSafetyLogin, Context.MODE_PRIVATE);

        if(!Information.sharedPreferences.getString(Information.emailPreference,"").equals("")
           && !Information.sharedPreferences.getString(Information.passwordPreference,"").equals(""))
        {
            if(Information.sharedPreferences.getString(Information.limitZone,"").equals("on"))
            {
                Intent sessionIntent =new Intent(this, LimitZoneActivity.class);
                // making sure activity stack is cleared before starting landing activity
               // sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(sessionIntent);
            }
            else
            {
                Intent sessionIntent = new Intent(this, MenuActivity.class);
                // making sure activity stack is cleared before starting landing activity
                sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(sessionIntent);
            }
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(Information.sharedPreferences.getString(Information.emailPreference,"")
                    , Information.sharedPreferences.getString(Information.passwordPreference,"")).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                } else {
                    Intent sessionIntent2 =new Intent(this, LoginActivity.class);
                    // making sure activity stack is cleared before starting landing activity
                    sessionIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(sessionIntent2);
                    Toast.makeText(MainActivity.this, "You need to login again, your session has been expired!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        login = findViewById(R.id.buttonLoginMain);
        createAccount = findViewById(R.id.buttoncreateAccounMain);

        login.setOnClickListener(r -> {
            Intent sessionIntent = new Intent(this, LoginActivity.class);
            // sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
             startActivity(sessionIntent);
        });

        createAccount.setOnClickListener(r -> {
            Intent sessionIntent = new Intent(this, CreateAccountActivity.class);
           // sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(sessionIntent);
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}