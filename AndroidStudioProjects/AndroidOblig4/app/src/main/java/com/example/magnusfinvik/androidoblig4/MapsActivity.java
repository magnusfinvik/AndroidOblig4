package com.example.magnusfinvik.androidoblig4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap;
    public SharedPreferences sharedPreferences;
    private ArrayList<LatLng> positionsList = null;
    public int locationCount = 0;
    LocationManager locationManager;

    private final int[] TYPES_OF_MAP = {GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_HYBRID,
                GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_TERRAIN,
                GoogleMap.MAP_TYPE_NONE,};

    private final int MAP_TYPE_NORMAL = 0;
    private final int MAP_TYPE_HYBRID = 1;
    private final int MAP_TYPE_SATELLITE = 2;
    private final int MAP_TYPE_TERRAIN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        positionsList = new ArrayList<LatLng>();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment fragMap = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = fragMap.getMap();

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);


        sharedPreferences = getSharedPreferences("location", 0);
        locationCount = sharedPreferences.getInt("locationCount", 0);
        String zoom = sharedPreferences.getString("zoom", "0");
        if(locationCount != 0){
            String lat = "";
            String lng = "";

            for(int i = 0; i < locationCount; i++){
                lat = sharedPreferences.getString("lat" + i, "0");
                lng = sharedPreferences.getString("lng" + i, "0");
                addMarkerToMap(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom)));
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng position) {
                locationCount++;
                addMarkerToMap(position);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lat" + Integer.toString((locationCount - 1)), Double.toString(position.latitude));
                editor.putString("lng" + Integer.toString((locationCount - 1)), Double.toString(position.longitude));
                editor.putInt("locationCount", locationCount);
                editor.putString("zoom", Float.toString(mMap.getCameraPosition().zoom));
                editor.apply();
            }
        });

    }

    private void addMarkerToMap(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng));
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        positionsList.add(latLng);
        polylineOptions.addAll(positionsList);
        mMap.addPolyline(polylineOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.normal:
                mMap.setMapType(TYPES_OF_MAP[MAP_TYPE_NORMAL]);
                itemIsChecked(item);
                return true;
            case R.id.hybrid:
                mMap.setMapType(TYPES_OF_MAP[MAP_TYPE_HYBRID]);
                itemIsChecked(item);
                return true;
            case R.id.satellite:
                mMap.setMapType(TYPES_OF_MAP[MAP_TYPE_SATELLITE]);
                itemIsChecked(item);
                return true;
            case R.id.terrain:
                mMap.setMapType(TYPES_OF_MAP[MAP_TYPE_TERRAIN]);
                itemIsChecked(item);
                return true;
            case R.id.deletePositions:
                deletePositions();
                return true;
            case R.id.exit:
                exit();
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit() {
        AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this).create();
        dialog.setTitle("Exit");
        dialog.setMessage("Do you want to exit the application?");
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void deletePositions() {
        AlertDialog dialog = new AlertDialog.Builder(MapsActivity.this).create();
        dialog.setTitle("Delete");
        dialog.setMessage("Do you want to delete all positionsList?");
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeAllPositions();
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    private void removeAllPositions() {
        mMap.clear();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        positionsList.clear();
        locationCount = 0;
    }

    private void itemIsChecked(MenuItem item) {
        if(item.isChecked()){
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
    }
}
