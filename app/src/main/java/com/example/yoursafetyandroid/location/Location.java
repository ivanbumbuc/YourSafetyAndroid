package com.example.yoursafetyandroid.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.widget.Toast;

import com.example.yoursafetyandroid.account.Information;
import com.example.yoursafetyandroid.main.MainActivity;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Location {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public Location()
    {
       // Location.locationDetect(MenuActivity.context);
        getLocation();
    }

    @SuppressLint("MissingPermission")
    public static void locationDetect(Context context)
    {
        FusedLocationProviderClient fusedLocationProviderClient;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                android.location.Location location=task.getResult();
                if(location!=null)
                {
                    try {
                        Geocoder geocoder = new Geocoder(MenuActivity.context, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        System.out.println(addresses.get(0).getLatitude() +" "+ addresses.get(0).getLongitude());
                        FirebaseAuth auth=FirebaseAuth.getInstance();
                        FirebaseUser user=auth.getCurrentUser();
                        FirebaseFirestore db=FirebaseFirestore.getInstance();// initializam instanta la baza de date
                        if(user!=null) {
                           addBaseLocationLive(user,db, addresses);
                        }
                           // Toast.makeText(MenuActivity.context, addresses.get(0).getLatitude()+", "+addresses.get(0).getLongitude()+", "+addresses.get(0).getCountryName()+", "+addresses.get(0).getLocality()+", "+addresses.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void addBaseLocation(FirebaseUser user, FirebaseFirestore db, List<Address> addresses)
    {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("HH:mm");
        Map<String,Object> info=new HashMap<>();
        info.put("Latitude",addresses.get(0).getLatitude());
        info.put("Longitude",addresses.get(0).getLongitude());
        info.put("Country",addresses.get(0).getCountryName());
        info.put("Locality",addresses.get(0).getLocality());
        info.put("Address",addresses.get(0).getAddressLine(0));
        db.collection("location_history").document(user.getUid()).collection(dtf.format(now)+"").document(dtf2.format(now)+"").set(info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                Toast.makeText(MenuActivity.context, "Location saves on database!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MenuActivity.context, "Error saves location try again later!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void addBaseLocationLive(FirebaseUser user, FirebaseFirestore db, List<Address> addresses)
    {
        Map<String,Object> info=new HashMap<>();
        info.put("latitude",addresses.get(0).getLatitude());
        info.put("longitude",addresses.get(0).getLongitude());
        db.collection("liveLocation").document(user.getUid()).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //Toast.makeText(MenuActivity.context, "Location shared!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MenuActivity.context, "Error saves location try again later!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getLocation() {
            if (locationCallback != null)
                fusedLocationClient.removeLocationUpdates(locationCallback);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(MenuActivity.context);

            locationRequest = LocationRequest.create();
            locationRequest.setInterval(2000); // Update interval in milliseconds
            locationRequest.setFastestInterval(2000); // Fastest update interval in milliseconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (android.location.Location location : locationResult.getLocations()) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            FirebaseUser user = auth.getCurrentUser();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();// initializam instanta la baza de date
                            if (user != null) {
                                Map<String, Object> info = new HashMap<>();
                                info.put("latitude", latitude);
                                info.put("longitude", longitude);
                                db.collection("liveLocation").document(user.getUid()).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //Toast.makeText(MenuActivity.context, "Location shared!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MenuActivity.context, "Error saves location try again later!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            System.out.println(latitude + " " + longitude + " --------------------------");
                        }
                    }
                }
            };
            startLocationUpdates();
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
