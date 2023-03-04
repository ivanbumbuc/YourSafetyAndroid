package com.example.yoursafetyandroid.menu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.yoursafetyandroid.R;
import com.example.yoursafetyandroid.location.TimeLocation;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;


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

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                    }
                });
                handler = new Handler();
                runnable = new RefreshData(map, handler);
                handler.postDelayed(runnable, 2000);
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
            MarkerOptions options = new MarkerOptions();
            options.title("current position");
            options.position(new LatLng(45.760696+Math.random(),21.226788+Math.random()));
            map.addMarker(options);
            handler.postDelayed(this,2000);
        }
    }


}
