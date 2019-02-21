package com.example.skear.trajectorytracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import static com.example.skear.trajectorytracker.GPSState.GPS_NOT_ENABLED;
import static com.example.skear.trajectorytracker.GPSState.READY;
import static com.example.skear.trajectorytracker.GPSState.REQUIRES_PERMISSION;

enum GPSState {
    READY, GPS_NOT_ENABLED, REQUIRES_PERMISSION
}

public class GPSTracker implements LocationListener {

    private Context context;
    private LocationManager locManager;


    public GPSTracker(Context context) {
        this.context = context;
    }

    public GPSState captureState() {
        GPSState state;
        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled) {
            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                state = REQUIRES_PERMISSION;
            }
            else {
                state = READY;
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                        0, this);
            }
        }
        else
            state = GPS_NOT_ENABLED;
        return state;
    }

    public Location getLocation() {
        Location location = null;
        try {
            location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        catch(SecurityException e) {
        }
        return location;
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
