package com.example.yoursafetyandroid.login;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth; //pentru conectare in cont
  //  private ProgressDialog progres;  //pentru progresul la conectare

    private  EditText email;
    private  EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        addButtonFunctionality();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    private void addButtonFunctionality() {
        ImageView backBtn = findViewById(R.id.buttonBackLogin);
        Button loginBtn = findViewById(R.id.buttonConnectLogin);

     //   progres = new ProgressDialog(this);
        auth= FirebaseAuth.getInstance();
        email = findViewById(R.id.inputEmailLogin);
        password = findViewById(R.id.inputPasswordLogin);
        loginBtn.setOnClickListener(v -> {
            // loginBtn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_press_animation));
            if (email.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
           //     progres.setMessage("Please wait while we set up");
            //    progres.setTitle("Login");
             //   progres.setCanceledOnTouchOutside(false);
               // progres.show();
                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                        editor.putString(Information.emailPreference, email.getText().toString());
                        editor.putString(Information.passwordPreference, password.getText().toString());
                        editor.putString(Information.userUIDPreference, Objects.requireNonNull(auth.getCurrentUser()).getUid());
                        editor.apply();
                     //   progres.dismiss();
                        logat();
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    } else {
                      //  progres.dismiss();
                        Toast.makeText(LoginActivity.this, "Login failed, email or password incorrect!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    private void logat()
    {
        Intent sessionIntent =new Intent(this, MenuActivity.class);
        // making sure activity stack is cleared before starting landing activity
        sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sessionIntent);
    }

}