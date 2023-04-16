package com.example.yoursafetyandroid.limitZone;

import static android.app.PendingIntent.getActivity;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.location.LocationService;
import com.example.yoursafetyandroid.menu.MenuActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LimitZoneActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private static FirebaseFirestore db;
    private static boolean isOnMap = false;
    private Button showDialogButton;
    private EditText textSearch;
    private Button searchButton;
    private Button clearMarkers;

    private Handler handlerChild;
    private Runnable runnableChild;
    private boolean clearMap = false;

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
                showDialogButton = findViewById(R.id.addButtonMapBox);
                showDialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAlertDialogWithEditText();
                    }
                });
                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull LatLng point) {
                        // Add a marker at the clicked location
                        double circleRadius = 80; // Change this value to set the circle's radius
                        if(!isOnMap && !isParent() && !isChildForParent()) {
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


                clearMarkers= findViewById(R.id.clearButtonMapBox);
                clearMarkers.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        map.clear();
                        removeCircle(mapboxMap);
                        showDialogButton.setEnabled(false);
                        isOnMap = false;
                    }
                });

            }
        });
        textSearch = (EditText)findViewById(R.id.textSearchLocationZone);
        searchButton = (Button)findViewById(R.id.buttonSearchLimitZone);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textSearch.getText().toString().length() > 0)
                {
                    MapboxGeocoding client = MapboxGeocoding.builder()
                            .accessToken(getString(R.string.mapbox_access_token))
                            .query(textSearch.getText().toString())
                            .build();

                    client.enqueueCall(new Callback<GeocodingResponse>() {
                        @Override
                        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                            List<CarmenFeature> results = response.body().features();
                            if (results.size() > 0) {
                                CarmenFeature feature = results.get(0);
                                LatLng location = new LatLng(
                                        feature.center().latitude(),
                                        feature.center().longitude()
                                );
                                CameraPosition position = new CameraPosition.Builder()
                                        .target(location) // The location you want to zoom to
                                        .zoom(12)
                                        .build();
                                map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                            }
                        }

                        @Override
                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                            showFailureDialog("Location was not founded!", "Please try again!");
                        }
                    });
                }
            }
        });

        handlerChild = new Handler();
        runnableChild = new Runnable() {
            @Override
            public void run() {
                // Your repeating task here
                if(isChildForParent())
                {
                    map.clear();
                    removeCircle(map);
                    DocumentReference docRef = db.collection("limitZone").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && (int)document.getData().get("type") != LimitType.NONE) {
                                if((int)document.getData().get("type") == LimitType.CHILD)
                                {
                                    double latitude = (double)document.getData().get("latitude");
                                    double longitude = (double)document.getData().get("longitude");
                                    LatLng point = new LatLng(latitude,longitude);
                                    addMarkerWithRedCircle(map, point, 100);
                                    CameraPosition position = new CameraPosition.Builder()
                                            .target(point) // The location you want to zoom to
                                            .zoom(17)
                                            .build();
                                    map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                                }
                            }
                        }
                    });

                    DocumentReference docRef2 = db.collection("liveLocation").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                    docRef2.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                    double latitude = (double)document.getData().get("latitude");
                                    double longitude = (double)document.getData().get("longitude");
                                    LatLng point = new LatLng(latitude,longitude);
                                    addMarkerChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),latitude,longitude,"Your location");
                                    CameraPosition position = new CameraPosition.Builder()
                                            .target(point) // The location you want to zoom to
                                            .zoom(17)
                                            .build();
                                    map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                            }
                        }
                    });
                    showDialogButton.setEnabled(false);
                    clearMarkers.setEnabled(false);
                    isOnMap = true;
                    clearMap = true;

                    boolean isMyServiceRunning = isServiceRunning(LocationService.class);
                    if (!isMyServiceRunning) {
                        Intent startServiceIntent = new Intent(MenuActivity.context, LocationService.class);
                        MenuActivity.context.startService(startServiceIntent);
                    }
//                    Intent serviceIntent = new Intent(MenuActivity.context, LocationService.class);
//                    MenuActivity.context.stopService(serviceIntent);
//                    MenuActivity.context.startService(serviceIntent);

                }
                else if(isParent())
                {
                 showDialogButton.setEnabled(false);
                 clearMarkers.setEnabled(true);
                 AtomicReference<String> uidChild= new AtomicReference<>("");
                    DocumentReference docRef = db.collection("limitZone").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                uidChild.set(document.getData().get("user").toString());
                            }
                        }
                    });
                 if(!Objects.equals(uidChild.get(), ""))
                 {
                     map.clear();
                     removeCircle(map);
                     DocumentReference docRef3 = db.collection("limitZone").document(uidChild.get());
                     docRef3.get().addOnCompleteListener(task -> {
                         if (task.isSuccessful()) {
                             DocumentSnapshot document = task.getResult();
                             if (document.exists() && (int)document.getData().get("type") != LimitType.NONE) {
                                 if((int)document.getData().get("type") == LimitType.CHILD)
                                 {
                                     double latitude = (double)document.getData().get("latitude");
                                     double longitude = (double)document.getData().get("longitude");
                                     LatLng point = new LatLng(latitude,longitude);
                                     addMarkerWithRedCircle(map, point, 100);
                                     CameraPosition position = new CameraPosition.Builder()
                                             .target(point) // The location you want to zoom to
                                             .zoom(17)
                                             .build();
                                     map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                                 }
                             }
                         }
                     });

                     DocumentReference docRef4 = db.collection("liveLocation").document(uidChild.get());
                     docRef4.get().addOnCompleteListener(task -> {
                         if (task.isSuccessful()) {
                             DocumentSnapshot document = task.getResult();
                             if (document.exists()) {
                                 double latitude = (double)document.getData().get("latitude");
                                 double longitude = (double)document.getData().get("longitude");
                                 LatLng point = new LatLng(latitude,longitude);
                                 addMarkerChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(),latitude,longitude,"Child location");
                                 CameraPosition position = new CameraPosition.Builder()
                                         .target(point) // The location you want to zoom to
                                         .zoom(17)
                                         .build();
                                 map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                             }
                         }
                     });
                 }
                }
                else
                {
                    clearMarkers.setEnabled(true);
                    if(clearMap) {
                        map.clear();
                        clearMap = false;
                    }
                    boolean isMyServiceRunning = isServiceRunning(LocationService.class);
                    if (isMyServiceRunning) {
                        Intent stopServiceIntent = new Intent(MenuActivity.context, LocationService.class);
                        MenuActivity.context.stopService(stopServiceIntent);
                    }
                }
                handlerChild.postDelayed(this, 3000); // 2000 milliseconds = 2 seconds
            }
        };
        handlerChild.post(runnableChild);


        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

    }

    private boolean isParent()
    {
        AtomicBoolean ok = new AtomicBoolean(false);
        DocumentReference docRef = db.collection("limitZone").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && (int)document.getData().get("type") != LimitType.NONE) {
                    if((int)document.getData().get("type") == LimitType.PARENT)
                        ok.set(true);
                }
            }
        });
        return ok.get();
    }

    private boolean isChildForParent()
    {
        AtomicBoolean ok = new AtomicBoolean(false);
        DocumentReference docRef = db.collection("limitZone").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists() && (int)document.getData().get("type") != LimitType.NONE) {
                    if((int)document.getData().get("type") == LimitType.CHILD)
                        ok.set(true);
                }
            }
        });
        return ok.get();
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

    private void addMarkerChild(String personUid, double latitude, double longitude,String title)
    {
        MarkerOptions options  = new MarkerOptions();
        if(Objects.equals(personUid, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            options.icon(drawableToIcon(MenuActivity.context,R.drawable.my_location_marker));
            options.setTitle(title);
            options.position(new LatLng(latitude, longitude));
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude)) // The location you want to zoom to
                    .zoom(13)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
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

                DocumentReference docRef = db.collection("liveLocation").document(userInput);
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String,Object> info=new HashMap<>();
                            info.put("typeMessage",1);
                            info.put("message","You need to accept zone limit request!");
                            db.collection("liveLocation").document(userInput).update(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MenuActivity.context, "Request send!", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MenuActivity.context, "Error send request!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else
                        {
                            showFailureDialog("Failure","The uid invalid!");
                        }
                    }
                });
                dialog.cancel();
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
        showDialogButton.setEnabled(true);
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


}