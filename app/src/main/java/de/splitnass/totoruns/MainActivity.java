package de.splitnass.totoruns;

import android.graphics.Color;
import android.icu.text.DateFormat;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap googleMap;

    private List<Location> locations = new ArrayList<>();

    private float totalDistance; // in meters

    private boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                .setInterval(5000)
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
        if (!isActive) {
            textView.setText("\nAccuracy : " + newLocation.getAccuracy());
        } else {
            Location lastLocation = locations.isEmpty() ? null : locations.get(locations.size() - 1);
            if (lastLocation != null) {
                totalDistance += newLocation.distanceTo(lastLocation);
            }
            locations.add(newLocation);
            StringBuilder message = new StringBuilder();
            message.append("\nAccuracy : " + newLocation.getAccuracy());
            message.append("\nTime : " + DateFormat.getDateTimeInstance().format(new Date(newLocation.getTime())));
            int secondsSinceLastLocation = (int) (lastLocation != null ? (newLocation.getTime() - lastLocation.getTime()) / 1000 : 0);
            message.append("\nSeconds since last Location: " + secondsSinceLastLocation);
            float distanceToLastLocation = lastLocation != null ? newLocation.distanceTo(lastLocation) : 0;
            message.append("\nDistance to last location: " + numberFormat.format(distanceToLastLocation));
            message.append("\nTotal distance : " + numberFormat.format(totalDistance));
            message.append("\nAltitude : " + numberFormat.format(newLocation.getAltitude()));
            message.append("\n\nSpeed in km/h: " + numberFormat.format((newLocation.getSpeed() * 3.6)));
            float calculatedSpeed = distanceToLastLocation / secondsSinceLastLocation;
            message.append("\n\nCalculated Speed in km/h: " + numberFormat.format(calculatedSpeed * 3.6));
            textView.setText(message.toString());

            PolylineOptions polylineOptions = new PolylineOptions().color(Color.RED).width(5);
            for (Location l : locations) {
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
        if (isActive) {
            isActive = false;
        } else {
            locations.clear();
            totalDistance = 0.0f;
            isActive = true;
        }
        Button button = (Button) findViewById(R.id.button3);
        button.setText(isActive ? "Stop" : "Start");
    }

    public boolean isActive() {
        return isActive;
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
