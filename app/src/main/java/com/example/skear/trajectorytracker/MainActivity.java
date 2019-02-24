package com.example.skear.trajectorytracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener, LocationListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION = 123;
    private ToggleButton captureToggleButton;
    private volatile boolean stopThread;
    private TextView currentLocationView;
    private Button viewDataBtn;
    private LocationManager locManager;
    private FileOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureToggleButton = (ToggleButton) findViewById(R.id.captureToggleButton);
        currentLocationView = (TextView) findViewById(R.id.currentLocationView);
        viewDataBtn = (Button) findViewById(R.id.viewDataBtn);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            checkPermission();
        File file = new File(this.getFilesDir(), "testFile");
        if(file.exists())
            file.delete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(outputStream != null) {
                outputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            try {
                outputStream = openFileOutput("testFile", Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            captureGpsThread();
        }
        else {
            stopThread = true;
            currentLocationView.setText("");
            viewDataBtn.setVisibility(View.VISIBLE);
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void captureGpsThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Location location = null;
                while(true) {
                    if(stopThread)
                        return;
                    try {
                        location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    catch(SecurityException e) {
                    }
                    if(location != null) {
                        String formattedLat = String.format(Locale.ENGLISH, "%.5f",
                                location.getLatitude());
                        String formattedLong = String.format(Locale.ENGLISH, "%.5f",
                                location.getLongitude());
                        final StringBuffer coords = new StringBuffer();
                        coords.append("LAT: ").append(formattedLat).append(" LONG: ")
                                .append(formattedLong);
                        Log.d(TAG, coords.toString());
                        StringBuffer data = new StringBuffer();
                        data.append(formattedLat).append(",").append(formattedLong)
                                .append("\n");
                        try {
                            outputStream.write(data.toString().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentLocationView.setText(coords);
                            }
                        });
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void checkPermission() {
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, this);
            captureToggleButton.setVisibility(View.VISIBLE);
            captureToggleButton.setOnCheckedChangeListener(this);
        } else {
            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Location permission is required to capture GPS " +
                        "data.", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                            0, this);
                    captureToggleButton.setVisibility(View.VISIBLE);
                    captureToggleButton.setOnCheckedChangeListener(this);
                }
            } else {
                Toast.makeText(this, "Permission was not granted.", Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onViewDataClick(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
