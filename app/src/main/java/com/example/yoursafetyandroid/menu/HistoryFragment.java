package com.example.yoursafetyandroid.menu;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.yoursafetyandroid.R;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HistoryFragment extends Fragment {

    private MapView mapView;
    private MapboxMap map;
    final Calendar myCalendar= Calendar.getInstance();
    private EditText editText;
    private TextView selectDate;
    private TextView historyView;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(MenuActivity.context, getString(R.string.mapbox_access_token));
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        mapView = (MapView)  rootView.findViewById(R.id.mapViewHistory);
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
            }
        });

        editText=(EditText)rootView.findViewById(R.id.dateHistory);
        historyView=(TextView) rootView.findViewById(R.id.historyView);
        selectDate = (TextView)rootView.findViewById(R.id.selectDate);
        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
                DocumentReference docRef = db
                        .collection("history")
                        .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, String> map2 = (Map) document.getData().get(editText.getText().toString().replace('/','-'));
                            if(map2 != null)
                            {
                                map.clear();
                                mapView.invalidate();
                                double latitude = 0;
                                double longitude = 0;
                                int i = 1;
                                List<MarkerOptions > markerOptionsList = new ArrayList<>();
                                for (Map.Entry<String,String> entry : map2.entrySet()) {
                                    String[] x = entry.getValue().split("-");
                                    latitude = Double.parseDouble(x[0]);
                                    longitude = Double.parseDouble(x[1]);
                                   markerOptionsList.add(addMarkerToMap(latitude,longitude, entry.getKey(), i));
                                    i++;
                                }
                                map.addMarkers(markerOptionsList);
                                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                                    case Configuration.UI_MODE_NIGHT_YES:
                                        selectDate.setTextColor(Color.WHITE);
                                        break;
                                    case Configuration.UI_MODE_NIGHT_NO:
                                        selectDate.setTextColor(Color.BLACK);
                                        break;
                                }
                                selectDate.setText("Below is the route!");
                            }
                            else
                            {
                                map.clear();
                                selectDate.setText("There is no history!");
                                selectDate.setTextColor(Color.RED);
                            }
                        }
                    }
                });
            }
        };
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(MenuActivity.context,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                historyView.setTextColor(Color.WHITE);
                editText.setTextColor(Color.WHITE);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                historyView.setTextColor(Color.BLACK);
                editText.setTextColor(Color.BLACK);
                break;
        }
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
        map.clear();
        editText.setText("");
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

    private void updateLabel(){
        String myFormat="MM/dd/yy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(dateFormat.format(myCalendar.getTime()));
    }

    private MarkerOptions addMarkerToMap(double latitude, double longitude,String hour,int number)
    {
        MarkerOptions options  = new MarkerOptions();
        if(number == 1) {
            options.title("Start: " + hour);
            options.icon(drawableToIcon(MenuActivity.context, R.drawable.start_history));
            options.position(new LatLng(latitude, longitude));
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude)) // The location you want to zoom to
                    .zoom(15) // The zoom level you want to set
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000); // 1000ms = 1s animation duration
        }
        else {
                options.title(hour);
                options.icon(drawableToIcon(MenuActivity.context, R.drawable.mid_history));
                options.position(new LatLng(latitude, longitude));
        }
       // map.addMarker(options);
        return options;
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