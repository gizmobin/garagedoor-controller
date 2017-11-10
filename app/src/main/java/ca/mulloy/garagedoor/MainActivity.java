package ca.mulloy.garagedoor;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.mulloy.garagedoor.utility.GPS;
import ca.mulloy.garagedoor.utility.IGPSActivity;

public class MainActivity extends AppCompatActivity implements IGPSActivity {
    private GPS gps;

    private TextView mLongitude;
    private TextView mLatitude;

    private double mLongitude_val;
    private double mLatitude_val;

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
                                    Toast.makeText(MainActivity.this, "Door goes up or down", Toast.LENGTH_LONG).show();

                                    new Thread(new Runnable(){
                                        @Override
                                        public void run() {
                                            sendAction( "toggle1" );
                                        }
                                    }).start();

                                }
                            }).show();
                }
            });
        }

        mLongitude_val = mLatitude_val = 0;
        mLongitude = (TextView) findViewById(R.id.longitude);
        mLatitude = (TextView) findViewById(R.id.latitude);

        gps = new GPS(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

    @Override
    protected void onResume() {
        if( !gps.isRunning() )
            gps.resumeGPS();

        super.onResume();
    }

    @Override
    protected void onStop() {
        gps.stopGPS();
        super.onStop();
    }

    @Override
    public void locationChanged(double longitude, double latitude) {
        mLongitude_val = longitude;
        mLatitude_val = latitude;
        runOnUiThread(changeLongLat);
    }

    private final Runnable changeLongLat = new Runnable() {
        @Override
        public void run() {
            mLongitude.setText( ""+mLongitude_val );
            mLatitude.setText("" + mLatitude_val );
        }
    };

    @Override
    public void displayGPSSettingsDialog() {
        Toast.makeText(MainActivity.this, "Please enable GPS", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void sendAction( String action ) {
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL("http://dev.mulloy.ca/garageDoor.php?action=" + action );
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
}
