package com.example.yoursafetyandroid.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText repeatPassword;
    private EditText name;
    private AutoCompleteTextView sex;
    private AutoCompleteTextView country;
    private TextView zipCode;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private Button sigIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        Information.sharedPreferences = getSharedPreferences(Information.yourSafetyLogin, Context.MODE_PRIVATE);

        init();

        sigIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!password.getText().toString().equals(repeatPassword.getText().toString()))
                {
                    showFailureDialog("Failure create account", "Repeat password are not the same with password!");
                }
                else
                {
                    checkEmailExistsOrNot(email.getText().toString());
                }
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void initVariable(){
        auth=FirebaseAuth.getInstance();
        email = findViewById(R.id.inputEmailSignIn);
        password = findViewById(R.id.inputPasswordSignIn);
        repeatPassword = findViewById(R.id.inputRepeatPasswordSignIn);
        name = findViewById(R.id.inputNameSignIn);
        sex = findViewById(R.id.inputSexSignIn);
        country = findViewById(R.id.inputCountrySignIn);
        zipCode = findViewById(R.id.inputZipCodeSignIn);
        sigIn = findViewById(R.id.buttonSignIn);
    }

    private void init()
    {
        initVariable();
        String[] items = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapterSex = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        sex.setAdapter(adapterSex);
        String[] countries = getResources().getStringArray(R.array.countries);
        ArrayAdapter<String> countriesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        country.setAdapter(countriesAdapter);

    }

    private void checkEmailExistsOrNot(String emailAddressString){

        auth.fetchSignInMethodsForEmail(emailAddressString).addOnCompleteListener(task -> {
            if (task.getResult().getSignInMethods().size() == 0){
                progressDialog=new ProgressDialog(this);
                registration();
            } else {
                showFailureDialog("Failure create account", "Email already exist!");
            }

        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private void showFailureDialog(String title,String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Set up the input
        final TextView input = new TextView(this);
        input.setText(text);
        input.setTextColor(Color.RED);
        builder.setView(input);

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#33ccff"));
    }

    private void registration()
    {
        SharedPreferences.Editor editor = Information.sharedPreferences.edit();
        editor.putString(Information.emailPreference, email.getText().toString());

        auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful())
                    {

                        editor.putString(Information.passwordPreference, password.getText().toString());
                        editor.putString(Information.userNamePreference, name.getText().toString());
                        editor.putString(Information.countryPreference, country.getText().toString());
                        editor.putString(Information.sexPreference, sex.getText().toString());
                        editor.putString(Information.zipCodePreference, zipCode.getText().toString());
                        editor.putString(Information.userUIDPreference, Objects.requireNonNull(auth.getCurrentUser()).getUid());
                        editor.commit();

                        addToDataBase();

                        Intent sessionIntent =new Intent(this, MenuActivity.class);
                        //sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(sessionIntent);

                        Toast.makeText(CreateAccountActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        progressDialog.dismiss();
                        Toast.makeText(CreateAccountActivity.this, "Error try again later!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToDataBase()
    {
        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("MM-dd-yy");
        String formattedDate = currentDate.format(formatterDate);

        //history
        Map<String,String> x = new HashMap<>();
        Map<String, Object> history = new HashMap<>();
        history.put(formattedDate, x);
        db.collection("history")
                .document(Objects.requireNonNull(uid))
                .set(history)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateAccountActivity.this, "Error adding document history!", Toast.LENGTH_SHORT).show();
                    }
                });

        //limitZone
        Map<String, Object> limitZone = new HashMap<>();
        limitZone.put("alert",false);
        limitZone.put("latitude", 0);
        limitZone.put("longitude",0);
        limitZone.put("type", 0);
        limitZone.put("user","");
        db.collection("limitZone")
                .document(Objects.requireNonNull(uid))
                .set(limitZone)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateAccountActivity.this, "Error adding document limitZone!", Toast.LENGTH_SHORT).show();
                    }
                });

        //liveLocation
        Map<String, Object> accountInformation = new HashMap<>();
        accountInformation.put("email", email.getText().toString());
        accountInformation.put("name", name.getText().toString());
        accountInformation.put("sex", sex.getText().toString());
        accountInformation.put("country", country.getText().toString());
        accountInformation.put("zipCode", zipCode.getText().toString());

        Map<String, Object> liveLocation = new HashMap<>();
        liveLocation.put("danger",false);
        liveLocation.put("fakeCall", false);
        liveLocation.put("historyLocation",false);
        liveLocation.put("icon", 0);
        liveLocation.put("latitude",0);
        liveLocation.put("longitude",0);
        liveLocation.put("message","");
        liveLocation.put("numberFakeCall","07222222222");
        liveLocation.put("shareLocation",false);
        liveLocation.put("type",0);
        liveLocation.put("typeMessage",0);
        liveLocation.put("user","");
        liveLocation.put("accountInformation",accountInformation);
        db.collection("liveLocation")
                .document(Objects.requireNonNull(uid))
                .set(liveLocation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateAccountActivity.this, "Error adding document liveLocation!", Toast.LENGTH_SHORT).show();
                    }
                });

        //locationsPersons
        Map<String, Object> locationsPersons = new HashMap<>();
        List<String> s=new ArrayList<>();
        locationsPersons.put("persons",s);
        db.collection("locationsPersons")
                .document(Objects.requireNonNull(uid))
                .set(locationsPersons)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateAccountActivity.this, "Error adding document locationsPersons!", Toast.LENGTH_SHORT).show();
                    }
                });

        //messageLive
        Map<String, Object> messageLive = new HashMap<>();
        messageLive.put("message","");
        messageLive.put("type", 1);
        db.collection("messageLive")
                .document(Objects.requireNonNull(uid))
                .set(messageLive)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateAccountActivity.this, "Error adding document messageLive!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}