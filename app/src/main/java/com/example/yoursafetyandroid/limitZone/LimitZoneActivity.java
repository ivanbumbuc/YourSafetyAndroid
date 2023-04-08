package com.example.yoursafetyandroid.limitZone;

import static android.app.PendingIntent.getActivity;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.main.MainActivity;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.turf.TurfMeasurement;

import java.util.Objects;

public class LimitZoneActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private static FirebaseFirestore db;
    private static boolean isOnMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(MenuActivity.context, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_limit_zone);
        db = FirebaseFirestore.getInstance();

        mapView = (MapView) findViewById(R.id.limitZoneMap);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                    }
                });
                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {
                        // Add a marker at the clicked location
                        double circleRadius = 80; // Change this value to set the circle's radius
                        if(!isOnMap) {
                            addMarkerWithRedCircle(mapboxMap, point, circleRadius);
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(point) // The location you want to zoom to
                                    .zoom(17)
                                    .build();
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                        }
                        return true;
                    }
                });

                Button showDialogButton = findViewById(R.id.addButtonMapBox);
                showDialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAlertDialogWithEditText();
                    }
                });

                Button clearMarkers= findViewById(R.id.clearButtonMapBox);
                clearMarkers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        map.clear();
                        removeCircle(mapboxMap);
                        isOnMap = false;
                    }
                });
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

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
        map.clear();
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

    private void showAlertDialogWithEditText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter UID code");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = input.getText().toString();
                // Process the user input here
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#33ccff"));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#33ccff"));
    }


    private void addMarkerWithRedCircle(MapboxMap mapboxMap, LatLng latLng, double circleRadius) {
        // Add a marker at the specified location
        IconFactory iconFactory = IconFactory.getInstance(LimitZoneActivity.this);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(iconFactory.defaultMarker());
        mapboxMap.addMarker(markerOptions);

        // Add a red circle around the marker
        GeoJsonSource circleSource = new GeoJsonSource("circle-source", Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        mapboxMap.getStyle().addSource(circleSource);


        CircleLayer circleLayer = new CircleLayer("circle-layer", "circle-source");
        circleLayer.setProperties(
                circleColor(Color.RED),
                circleRadius((float) circleRadius),
                circleOpacity(0.5f)
        );
        mapboxMap.getStyle().addLayer(circleLayer);
        isOnMap = true;
    }

    private void removeCircle(MapboxMap mapboxMap) {
        // Get the map style
        Style style = mapboxMap.getStyle();

        if (style != null) {
            // Remove the circle layer
            Layer circleLayer = style.getLayer("circle-layer");
            if (circleLayer != null) {
                style.removeLayer(circleLayer);
            }

            // Remove the circle source
            Source circleSource = style.getSource("circle-source");
            if (circleSource != null) {
                style.removeSource(circleSource);
            }
        }
    }

    private double calculateDistance(LatLng latLng1, LatLng latLng2) {
        // Create Location objects from LatLng
        Location location1 = new Location("Location 1");
        location1.setLatitude(latLng1.getLatitude());
        location1.setLongitude(latLng1.getLongitude());

        Location location2 = new Location("Location 2");
        location2.setLatitude(latLng2.getLatitude());
        location2.setLongitude(latLng2.getLongitude());

        // Calculate the distance in meters
        return location1.distanceTo(location2);
    }


}