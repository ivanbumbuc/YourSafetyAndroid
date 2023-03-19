package com.example.yoursafetyandroid.menu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.location.TimeLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class LocationFragment extends Fragment {

    private Handler handler;
    private Runnable runnable;
    private MapView mapView;
    private MapboxMap map;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(MenuActivity.context, getString(R.string.mapbox_access_token));
        if( ActivityCompat.checkSelfPermission(MenuActivity.context, Manifest.permission.ACCESS_BACKGROUND_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            TimeLocation time = new TimeLocation(MenuActivity.context);
            time.cancelTime();
            time.setTime();
        }
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location, container, false);
        mapView = (MapView)  rootView.findViewById(R.id.mapView2);
        mapView.onCreate(savedInstanceState);
        mapView.invalidate();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        handler = new Handler();
                        runnable = new RefreshData(map, handler);
                        handler.postDelayed(runnable, 2);
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        DocumentReference docRef = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                        docRef.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    CameraPosition position = new CameraPosition.Builder()
                                            .target(new LatLng((double)document.getData().get("latitude"), (double)document.getData().get("longitude"))) // The location you want to zoom to
                                            .zoom(13) // The zoom level you want to set
                                            .build();
                                    map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000); // 1000ms = 1s animation duration
                                }
                            }
                        });
                    }
                });
            }
        });
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mapView.onStop();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    private class RefreshData implements Runnable {
        private MapboxMap map;
        private Handler handler;
        public  RefreshData(MapboxMap map, Handler handler)
        {
            this.map = map;
            this.handler = handler;
        }


        @Override
        public void run() {
            map.clear();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("locationsPersons").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists() && document.getData().get("persons") != null) {
                        List<String> persons = (ArrayList<String>)Objects.requireNonNull(document.getData().get("persons"));
                        for (String person :persons) {
                           addMarkerAndGetLocation(db, person);
                        }
                    }
                }
            });
            addMarkerAndGetLocation(db, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            handler.postDelayed(this,4000);

        }
    }

    private void addMarkerAndGetLocation(FirebaseFirestore db,String personUid)
    {
        DocumentReference docRef = db.collection("liveLocation").document(personUid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && ((boolean)document.getData().get("shareLocation") || personUid ==  Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                    addMarkerToMap(personUid,(double)document.getData().get("latitude"), (double)document.getData().get("longitude"),Integer.parseInt(document.getData().get("icon").toString()),(boolean) document.getData().get("danger"));
                }
            }
        });
    }

    private void addMarkerToMap(String personUid, double latitude, double longitude,int number, boolean danger)
    {
        MarkerOptions options  = new MarkerOptions();
        if(Objects.equals(personUid, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            options.title("Your location");
           options.icon(drawableToIcon(MenuActivity.context,R.drawable.my_location_marker));
           options.position(new LatLng(latitude, longitude));
        }
        else
        {
            options.position(new LatLng(latitude + Math.random(), longitude + Math.random()));
            if (danger) {
                options.title("I am in danger, i need help!!");
                options.icon(drawableToIcon(MenuActivity.context,R.drawable.help_location));
            }
            else {
                options.title(personUid);
                options.icon(drawableToIcon(MenuActivity.context,R.drawable.people_location_marker));
            }
        }
        map.addMarker(options);
    }

    public static Icon drawableToIcon(@NonNull Context context, @DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), id, context.getTheme());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return IconFactory.getInstance(context).fromBitmap(bitmap);
    }
}
