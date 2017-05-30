package com.blisek.googlemapssample;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.blisek.googlemapssample.structs.CityPosition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    @BindView(R.id.cities_list)
    Spinner citiesList;

    @BindView(R.id.navigation_type)
    Spinner navigationType;

    private List<CityPosition> currentRoute = new ArrayList<>();
    private CityPosition currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initCitiesList();
    }

    private void initCitiesList() {
        CityPosition[] cityPositions = null;
        try(InputStream raw = getResources().openRawResource(R.raw.miasta)) {
            try(Reader is = new BufferedReader(new InputStreamReader(raw, "UTF8"))) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                cityPositions = gson.fromJson(is, CityPosition[].class);
            }
        }
        catch (IOException ioe) {
            Log.e(TAG, "Error while reading raw resource", ioe);
            return;
        }

        if(cityPositions == null) {
            Log.w(TAG, "Cities list is empty");
            return;
        }

        ArrayAdapter<CityPosition> adapter = new ArrayAdapter<CityPosition>(this,
                android.R.layout.simple_spinner_item, cityPositions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citiesList.setAdapter(adapter);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(51.107885, 17.038538);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

    }

    @OnClick(R.id.btn_show_city)
    public void onShowCityButtonClicked(View view) {
        currentCity = (CityPosition)citiesList.getSelectedItem();
        mMap.clear();
        markCity();
    }

    @OnClick(R.id.btn_add_to_route)
    public void onAddToRootButtonClicked(View view) {
        CityPosition cp = (CityPosition)citiesList.getSelectedItem();
        currentCity = cp;
        currentRoute.add(cp);
        redrawMap();
    }

    @OnClick(R.id.btn_clear_path)
    public void onClearRouteButtonClicked(View view) {
        currentRoute.clear();
        redrawMap();
    }

    @OnClick(R.id.btn_navigate)
    public void onNavigationButtonClicked(View view) {
        int currentRouteSize = currentRoute.size();
        if(currentRouteSize < 2)
            return;
        String travelMode = (String)navigationType.getSelectedItem();
        final String navigationUrl =
                "https://www.google.com/maps/dir/?api=1&origin=%s,%s&destination=%s,%s&travelmode=%s";
        CityPosition origin = currentRoute.get(0), destination = currentRoute.get(currentRouteSize-1);
        String navigationUriString = String.format(navigationUrl,
                Float.toString(origin.latitude), Float.toString(origin.longtitude),
                Float.toString(destination.latitude), Float.toString(destination.longtitude), travelMode);
        if(currentRouteSize > 2)
            navigationUriString += String.format("&waypoints=%s", joinIntermediateWaypoints());
        Intent navigation = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUriString));
        startActivity(navigation);
    }

    private String joinIntermediateWaypoints() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for(int i = 1; i < currentRoute.size()-1; ++i) {
            if(first)
                first = false;
            else
                sb.append('|');
            CityPosition cp = currentRoute.get(i);
            sb.append(Float.toString(cp.latitude));
            sb.append(',');
            sb.append(Float.toString(cp.longtitude));
        }

        return sb.toString();
    }

    void redrawMap() {
        mMap.clear();
        drawPath();
        markCity();
    }

    void markCity() {
        if(currentCity == null)
            return;

        CityPosition cp = currentCity;
        LatLng marker = new LatLng(cp.latitude, cp.longtitude);
        mMap.addMarker(new MarkerOptions().position(marker).title(cp.city));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13));
    }

    void drawPath() {
        if(currentRoute.size() < 2)
            return;

        PolylineOptions polylineOptions = new PolylineOptions().geodesic(true);
        for(CityPosition cp : currentRoute)
            polylineOptions.add(new LatLng(cp.latitude, cp.longtitude));

        mMap.addPolyline(polylineOptions);
    }
}
