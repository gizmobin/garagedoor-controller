package ca.mulloy.garagedoor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    private final static int FINE_LOCATION = 100;
    private boolean mMyLocationEnabled;

    private TextView mLongitude;
    private TextView mLatitude;
    private TextView mDistanceTxt;

    private Double mLongitude_val;
    private Double mLatitude_val;
    private Double mMetres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.toggle);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Toggle garage door", Snackbar.LENGTH_LONG)
                            .setAction("Toggle", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mMetres < 500) {
                                        Toast.makeText(MainActivity.this, "Door goes up or down", Toast.LENGTH_LONG).show();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                sendAction("toggle1");
                                            }
                                        }).start();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Not close enough, sorry!", Toast.LENGTH_LONG).show();
                                    }

                                }
                            }).show();
                }
            });
        }

        mLongitude = (TextView) findViewById(R.id.longitude);
        mLatitude = (TextView) findViewById(R.id.latitude);

        mDistanceTxt = (TextView) findViewById(R.id.distanceDetail);
        mMetres = null;

        mDistanceTxt.setText("Application requires GPS location.");
        mLongitude.setText( "undefined" );
        mLatitude.setText( "");

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    Location location = locationList.get(locationList.size() - 1);

                    mLongitude_val = location.getLongitude();
                    mLatitude_val = location.getLatitude();
                    runOnUiThread(changeLongLat);
                }
            };
        };

        registerForLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if( mFusedLocationClient != null ) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMyLocationEnabled) {
            registerForLocation();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void calculateDistance() {

        String distance = "";
        double lat = 45.1683565;    // 254 Borden road
        double lng = -76.12503879999997;

        if (mLatitude_val != null && mLongitude_val != null) {
            mMetres = meterDistanceBetweenPoints(lat, lng, mLatitude_val, mLongitude_val);

            if (mMetres < 501) {
                distance = String.format(Locale.US, "%d", mMetres.intValue() ) + " m";
            } else {
                distance = String.format(Locale.US, "%.2f", (mMetres / 1000)) + " km";
            }
        }
        String message = "Distance from garage " + distance;
        mDistanceTxt.setText(message);
    }

    public static double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        float pk = (float) (180.f/Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    private void sendAction( String action ) {
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL("http://homeautomation.mulloy.ca/garageDoor.php?action=" + action );
            urlConnection = (HttpURLConnection)url.openConnection();

            InputStream in = urlConnection.getInputStream();
            InputStreamReader isw = new InputStreamReader(in);

            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                System.out.print(current);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private final Runnable changeLongLat = new Runnable() {
        @Override
        public void run() {
            mLongitude.setText( "" + mLongitude_val );
            mLatitude.setText( "" + mLatitude_val );

            calculateDistance();
        }
    };

    public void registerForLocation() {
        mMyLocationEnabled = false;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMyLocationEnabled = true;
            } else {
                requestPermission();
            }
        } else {

            mMyLocationEnabled = true;
        }

        if( mMyLocationEnabled ) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10 * 1000);
            locationRequest.setFastestInterval(1 * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient( this );
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == FINE_LOCATION ) {
            // If request is cancelled, the result arrays are empty.
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    registerForLocation();
                }
            }
            else {
                Toast.makeText(this, "Permission required to detect your location", Toast.LENGTH_LONG).show();
                mMyLocationEnabled = false;
            }
        }
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has MapView_Fragment shown
                                ActivityCompat.requestPermissions( MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION );
                            }
                        })
                        .create()
                        .show();
            }
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION );
            }
        }
    }
}
