package de.splitnass.totoruns;

import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap googleMap;

    private Run run;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        run = ((RunApplication)getApplication()).getRun();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        locationUpdated(location);
                    }
                }
            }

        };

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest mLocationRequest = LocationRequest.create()
                .setInterval(2)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                makeToast("Got initial Location");
                                locationUpdated(location);
                            }
                        }
                    });


            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    static {
        numberFormat.setMaximumFractionDigits(2);
    }

    private void locationUpdated(Location newLocation) {
        if (newLocation == null) return;

        googleMap.clear();
        LatLng loc = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        googleMap.addMarker(new MarkerOptions().position(loc));
        TextView textView = (TextView) findViewById(R.id.firstText);
        if (run.isActive()) {
            run.addLocation(newLocation);
        }

        StringBuilder message = new StringBuilder();
        message.append("Accuracy : " + newLocation.getAccuracy() + "\tReadings: " + run.getLocations().size());
        message.append("\nDuration : " + run.getDurationString());
        message.append("\nDistance : " + run.getTotalDistanceString());
        message.append("\nSpeed : " + run.getLastSpeedString());
        message.append("\nKM-Pace : " + run.getKilometerPaceString());
        message.append("\nPace : " + run.getPaceString());
        textView.setText(message.toString());

        if (run.isActive()) {
            PolylineOptions polylineOptions = new PolylineOptions().color(Color.RED).width(5);
            for (Location l : run.getLocations()) {
                polylineOptions.add(new LatLng(l.getLatitude(), l.getLongitude()));
            }
            googleMap.addPolyline(polylineOptions);
        }
    }

    private void makeToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        Log.i("TotoRuns", text);
    }

    public void toggleActive(View view) {
        if (run.isActive()) {
            run.stop();
        } else {
            run.start();
        }
        Button button = (Button) findViewById(R.id.button3);
        button.setText(run.isActive() ? "Stop" : "Start");
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Toast.makeText(getApplicationContext(), "Map is ready", Toast.LENGTH_SHORT).show();
    }



}
