package com.example.yoursafetyandroid.locationHistory;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class HistoryLocation {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public HistoryLocation() {
        getLocation();
    }

    private void getLocation() {
        if (locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MenuActivity.context);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update interval in milliseconds
        locationRequest.setFastestInterval(5000); // Fastest update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        updateDataBaseHistory(latitude, longitude);
                    }
                }
            }
        };
        startLocationUpdates();
    }

    public void updateDataBaseHistory(double latitude, double longitude) {
        //date
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("MM-dd-yy");
        String formattedDate = currentDate.format(formatterDate);
        //time
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("h:mm");
        String formattedTime = currentTime.format(formatterTime);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("history").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && document.getData().get(formattedDate) != null) {
                    Map<String, Object> historyDay = (Map<String, Object>) Objects.requireNonNull(document.getData().get(formattedDate));
                    historyDay.put(formattedTime, latitude + "-" + longitude);
                    Map<String, Object> updatesMap = new HashMap<>();
                    updatesMap.put(formattedDate, historyDay);
                    db.collection("history").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).update(updatesMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //Toast.makeText(MenuActivity.context, "Location shared!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MenuActivity.context, "Error saves history locations, try again later!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Map<String, Object> updates = new HashMap<>();
                    Map<String, String> field = new HashMap<>();
                    field.put(formattedTime, latitude + "-" + longitude);
                    updates.put(formattedDate, field);
                    // Update the document with the new field
                    Task<Void> future = docRef.update(updates);

                    // Wait for the operation to complete and print the result
                    future.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Handle success
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });

                }
            }
        });
    }



    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(MenuActivity.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MenuActivity.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

}
