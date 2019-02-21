package com.example.skear.trajectorytracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        File file = new File(this.getFilesDir(), "testFile");
        if(file.exists()) {
            readData();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Polyline polyline1 = googleMap.addPolyline(polylineOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylineOptions.getPoints()
                .get(0), 13));
    }

    private void readData() {
        polylineOptions = new PolylineOptions();
        try {
            FileInputStream fileInputStream = openFileInput("testFile");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String readString = bufferedReader.readLine();
            while(readString != null) {
                String[] parts = readString.split(",");
                polylineOptions.add(new LatLng(Double.parseDouble(parts[0]),
                        Double.parseDouble(parts[1])));
                readString = bufferedReader.readLine();
            }
            inputStreamReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
