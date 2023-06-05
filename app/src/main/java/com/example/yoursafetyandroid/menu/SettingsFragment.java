package com.example.yoursafetyandroid.menu;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.Utils.ListAdapterPersons;
import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.limitZone.LimitZoneService;
import com.example.yoursafetyandroid.location.LocationService;
import com.example.yoursafetyandroid.location.TimeLocation;
import com.example.yoursafetyandroid.locationHistory.HistoryLocation;
import com.example.yoursafetyandroid.locationHistory.HistoryLocationService;
import com.example.yoursafetyandroid.login.LoginActivity;
import com.example.yoursafetyandroid.main.MainActivity;
import com.example.yoursafetyandroid.pushNotification.NotificationService;
import com.example.yoursafetyandroid.safetyTimer.TimerService;
import com.example.yoursafetyandroid.sms.SmsService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


public class SettingsFragment extends Fragment {
    private EditText email;
    private EditText name;
    private EditText phone;
    private AutoCompleteTextView sex;
    private AutoCompleteTextView country;
    private TextView zipCode;
    private EditText uid;
    private FirebaseAuth auth;
    private Button applyChanges;
    private Button logOut;
    private TextView nameTitle;
    private FirebaseFirestore db;
    private ImageButton copy;

    //persons
    public static ListView listView;
    public static ArrayList<String> persons;
    public static ListAdapterPersons adapter;
    private ImageView addButton;
    private EditText dateInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        email = rootView.findViewById(R.id.inputEmailSettings);
        name = rootView.findViewById(R.id.inputNameSettings);
        phone = rootView.findViewById(R.id.editPhoneSettings);
        sex = rootView.findViewById(R.id.inputSexSettings);
        country = rootView.findViewById(R.id.inputCountrySettings);
        zipCode = rootView.findViewById(R.id.inputZipCodeSettings);
        applyChanges = rootView.findViewById(R.id.changeSettings);
        logOut = rootView.findViewById(R.id.logOut);
        uid = rootView.findViewById(R.id.inputUidSettings);
        nameTitle = rootView.findViewById(R.id.textViewSettingsName);
        db = FirebaseFirestore.getInstance();
        applyChanges = rootView.findViewById(R.id.changeSettings);
        logOut = rootView.findViewById(R.id.logOut);
        copy = rootView.findViewById(R.id.copySettingsGuid);
        addButton=rootView.findViewById(R.id.addPersons);
        dateInput=rootView.findViewById(R.id.inputPersons);
        listView=rootView.findViewById(R.id.listPersons);
        init();
        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setApplyChanges();
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLogOut();
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UID", Information.sharedPreferences.getString(Information.userUIDPreference, ""));
                clipboard.setPrimaryClip(clip);
            }
        });

        addPersonsFunction();

        addButton.setOnClickListener(view -> {
            String text=dateInput.getText().toString();
            if(text.length() == 0)
            {
                Toast.makeText(MenuActivity.context, "Insert a uid!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                persons.add(text);
                dateInput.setText("");
                Toast.makeText(MenuActivity.context, "Added: "+text, Toast.LENGTH_SHORT).show();
                listView.setAdapter(adapter);
                addPersonsDataBase();
            }
        });


        return rootView;
    }

    private void init()
    {
        email.setText(Information.sharedPreferences.getString(Information.emailPreference, ""));
        name.setText(Information.sharedPreferences.getString(Information.userNamePreference, ""));
        phone.setText(Information.sharedPreferences.getString(Information.phoneNumberPreference, ""));
        zipCode.setText(Information.sharedPreferences.getString(Information.zipCodePreference, ""));
        country.setText(Information.sharedPreferences.getString(Information.countryPreference, ""));
        sex.setText(Information.sharedPreferences.getString(Information.sexPreference, ""));
        uid.setText(Information.sharedPreferences.getString(Information.userUIDPreference, ""));
        nameTitle.setText("Welcome back, "+Information.sharedPreferences.getString(Information.userNamePreference, "")+"!");
    }

    private void setApplyChanges()
    {
        SharedPreferences.Editor editor = Information.sharedPreferences.edit();
        editor.putString(Information.userNamePreference,name.getText().toString());
        editor.putString(Information.sexPreference,sex.getText().toString());
        editor.putString(Information.phoneNumberPreference, phone.getText().toString());
        editor.putString(Information.zipCodePreference, zipCode.getText().toString());
        editor.putString(Information.countryPreference, country.getText().toString());
        editor.apply();

        Map<String,Object> z = new HashMap<>();
        Map<String,String> info = new HashMap<>();
        info.put("phone",phone.getText().toString());
        info.put("name",name.getText().toString());
        info.put("country",country.getText().toString());
        info.put("sex",sex.getText().toString());
        info.put("zipCode",zipCode.getText().toString());
        z.put("accountInformation", info);

        db.collection("liveLocation").document(Information.sharedPreferences.getString(Information.userUIDPreference, "")).update(z).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(MenuActivity.context, "Information has been saved!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MenuActivity.context, "Error save information!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLogOut()
    {
        SharedPreferences.Editor editor = Information.sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent sessionIntent = new Intent(MenuActivity.context, MainActivity.class);
        sessionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sessionIntent);
        stopServiceClass();
        Toast.makeText(MenuActivity.context, "Log out successful!", Toast.LENGTH_SHORT).show();
    }

    private void stopServiceClass()
    {
        boolean locationService = isServiceRunning(LocationService.class);
        if (locationService) {
            Intent stopServiceIntent = new Intent(MenuActivity.context, LocationService.class);
            MenuActivity.context.stopService(stopServiceIntent);
        }

        boolean limitZoneService = isServiceRunning(LimitZoneService.class);
        if (limitZoneService) {
            Intent stopServiceIntent = new Intent(MenuActivity.context, LimitZoneService.class);
            MenuActivity.context.stopService(stopServiceIntent);
        }

        boolean historyLocation = isServiceRunning(HistoryLocationService.class);
        if(historyLocation)
        {
            Intent stopServiceIntent = new Intent(MenuActivity.context, HistoryLocationService.class);
            MenuActivity.context.stopService(stopServiceIntent);
        }

        boolean notificationService = isServiceRunning(NotificationService.class);
        if(notificationService)
        {
            Intent stopServiceIntent = new Intent(MenuActivity.context, NotificationService.class);
            MenuActivity.context.stopService(stopServiceIntent);
        }

        boolean timerService = isServiceRunning(TimerService.class);
        if(timerService)
        {
            Intent stopServiceIntent = new Intent(MenuActivity.context, TimerService.class);
            MenuActivity.context.stopService(stopServiceIntent);
        }

    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        if (getActivity() != null) {
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addPersonsFunction()
    {
        personsInit();
    }
    public static void deletePerson(int i)
    {
        persons.remove(i);
        listView.setAdapter(adapter);
    }

    private void personsInit(){
        persons=new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("locationsPersons").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && document.getData().get("persons") != null) {
                    List<String> x = (ArrayList<String>)document.getData().get("persons");
                    persons.addAll(x);
                    adapter=new ListAdapterPersons(MenuActivity.context, persons);
                    listView.setAdapter(adapter);
                }
            } else {
                Log.d("getContact", "get failed with ", task.getException());
            }
        });
    }

    private void addPersonsDataBase()
    {
        Map<String,Object> x = new HashMap<>();
        x.put("persons",persons);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locationsPersons").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .update(x).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MenuActivity.context, "Error add persons!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}