package com.example.yoursafetyandroid.login;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.example.yoursafetyandroid.pushNotification.NotificationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth; //pentru conectare in cont
    private ProgressDialog progres;  //pentru progresul la conectare

    private  EditText email;
    private  EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Information.sharedPreferences = getSharedPreferences(Information.yourSafetyLogin, Context.MODE_PRIVATE);
        addButtonFunctionality();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    private void addButtonFunctionality() {
        Button loginBtn = findViewById(R.id.buttonConnectLogin);

        progres = new ProgressDialog(this);
        auth= FirebaseAuth.getInstance();
        email = findViewById(R.id.inputEmailLogin);
        password = findViewById(R.id.inputPasswordLogin);
        loginBtn.setOnClickListener(v -> {
            if (email.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
                progres.setMessage("Please wait while we set up");
                progres.setTitle("Login");
                progres.setCanceledOnTouchOutside(false);
                progres.show();
                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = Information.sharedPreferences.edit();
                        editor.putString(Information.emailPreference, email.getText().toString());
                        editor.putString(Information.passwordPreference, password.getText().toString());
                        editor.putString(Information.userUIDPreference, Objects.requireNonNull(auth.getCurrentUser()).getUid());
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        docRef.get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                DocumentSnapshot document = task2.getResult();
                                if (document.exists()) {
                                    Map<String, String> x = (Map<String, String>)document.getData().get("accountInformation");
                                    editor.putString(Information.userNamePreference,x.get("name"));
                                    editor.putString(Information.sexPreference,x.get("sex"));
                                    editor.putString(Information.phoneNumberPreference, x.get("phone"));
                                    editor.putString(Information.zipCodePreference, x.get("zipCode"));
                                    editor.putString(Information.countryPreference, x.get("country"));
                                    editor.apply();
                                }
                            }
                        });
                        editor.apply();
                        progres.dismiss();
                        logat();
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        boolean isMyServiceRunning = isServiceRunning(NotificationService.class);
                        if (isMyServiceRunning) {
                            Intent startServiceIntent = new Intent(this, NotificationService.class);
                            stopService(startServiceIntent);
                        }
                    } else {
                        progres.dismiss();
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}