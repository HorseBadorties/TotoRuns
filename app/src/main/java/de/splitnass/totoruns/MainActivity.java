package de.splitnass.totoruns;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements RunListener, OnMapReadyCallback, SensorEventListener {

    private GoogleMap googleMap;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private Run run;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        run = new Run(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MapFragment mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "resuming");
        run.addRunListener(this);
        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MainActivity", "pausing");
        run.removeRunListener(this);
        mSensorManager.unregisterListener(this);
    }

    public void locationUpdated(Location newLocation) {
        if (newLocation == null) return;


        googleMap.clear();
        LatLng loc = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        googleMap.addMarker(new MarkerOptions().position(loc));
        if (googleMap.getCameraPosition().zoom < 10) {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }
        TextView textView = (TextView) findViewById(R.id.firstText);

        StringBuilder message = new StringBuilder();
        message.append(String.format("Accuracy : %d (%tT) - Readings : %d",
                (int)newLocation.getAccuracy(),
                newLocation.getTime(),
                run.getLocations().size()));
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


    public void toggleActive(View view) {
        if (run.isActive()) {
            run.stop();
            new AsyncTask<Run, Object, Integer>() {
                @Override
                protected Integer doInBackground(Run... params) {
                    downloadGPX(MainActivity.this.getApplicationContext(), params[0]);
                    return 1;
                }
                @Override
                protected void onPostExecute(Integer result) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "GPX file saved",
                            Toast.LENGTH_LONG).show();
                }

            }.execute(run);

        } else {
            run.start();
        }
        Button button = (Button) findViewById(R.id.button3);
        button.setText(run.isActive() ? "Stop" : "Start");
    }

    private static void downloadGPX(Context context, Run r) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String timestamp = DateFormat.getDateTimeInstance().format(new Date(r.getEnd()));
        File xmlFile = new File(dir, "TotoRuns-" + timestamp + ".gpx");
        FileOutputStream outputStream;
        try {
            if (xmlFile.exists()) {
                xmlFile.delete();
            }
            outputStream = new FileOutputStream(xmlFile);
            outputStream.write(r.asGPX().getBytes());
            outputStream.flush();
            outputStream.close();
            DownloadManager dm = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            dm.addCompletedDownload(xmlFile.getName(),
                    "TotoRuns GPX", false, "gpx", xmlFile.getAbsolutePath(), 1, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        Toast.makeText(getApplicationContext(), "Map is ready", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i("Accelerometer", "Movement x-axis: " + Math.abs(event.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
