package ca.mulloy.garagedoor.utility;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPS {

    private IGPSActivity main;

    // Helper for GPS-Position
    private LocationListener mlocListener;
    private LocationManager mlocManager;

    private boolean isRunning;

    public GPS(IGPSActivity main) {
        this.main = main;

        // GPS Position
        mlocManager = (LocationManager) ((Activity) this.main).getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        try {
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
            this.isRunning = true;
        } catch (SecurityException sex) {
            this.isRunning = false;
        }
    }

    public void stopGPS() {
        if (isRunning) {
            try {
                mlocManager.removeUpdates(mlocListener);
            } catch (SecurityException sex) {
            }
            this.isRunning = false;
        }
    }

    public void resumeGPS() {
        try {
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
            this.isRunning = true;
        } catch (SecurityException sex) {
            this.isRunning = false;
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public class MyLocationListener implements LocationListener {

        private final String TAG = MyLocationListener.class.getSimpleName();

        @Override
        public void onLocationChanged(Location loc) {
            GPS.this.main.locationChanged(loc.getLongitude(), loc.getLatitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
            GPS.this.main.displayGPSSettingsDialog();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}

