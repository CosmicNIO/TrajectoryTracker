package com.example.skear.trajectorytracker;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
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
        implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MainActivity";
    private ToggleButton captureToggleButton;
    private volatile boolean stopThread;
    private GPSTracker gpsTracker;
    private TextView currentLocationView;
    private Button viewDataBtn;
    FileOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureToggleButton = (ToggleButton) findViewById(R.id.captureToggleButton);
        currentLocationView = (TextView) findViewById(R.id.currentLocationView);
        viewDataBtn = (Button) findViewById(R.id.viewDataBtn);
        gpsTracker = new GPSTracker(getApplicationContext());
        switch(gpsTracker.captureState()) {
            case GPS_NOT_ENABLED:
                Toast.makeText(this, "Enable 'Use Location' in settings.",
                        Toast.LENGTH_LONG).show();
                break;
            case REQUIRES_PERMISSION:
                Toast.makeText(this, "Please grant location permission",
                        Toast.LENGTH_LONG).show();
                break;
            case READY:
                captureToggleButton.setOnCheckedChangeListener(this);
        }
        File file = new File(this.getFilesDir(), "testFile");
        if(file.exists())
            file.delete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            stopThread = false;
            try {
                outputStream = openFileOutput("testFile", Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Location location;
                    while(true) {
                        if(stopThread)
                            return;
                        location = gpsTracker.getLocation();
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

    public void onViewDataClick(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}
