package de.splitnass.totoruns;


import android.app.Application;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Run {

    public Run(Application app) {
        this.app = app;
    }

    private Application app;
    private long start, end;
    private List<Location> locations = new ArrayList<>();
    private float totalDistance; // in meters
    private long beginCurrentKilometer;
    private float currentKilometerDistance;
    private List<Long> pacePerKilometer = new ArrayList<>();


    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    static {
        numberFormat.setMaximumFractionDigits(2);
    }
    private static SimpleDateFormat paceFormatter = new SimpleDateFormat("mm:ss");
    private static Calendar calendar = new GregorianCalendar();

    public long getStart() {
        return start;
    }

    public void start() {
        locations.clear();
        totalDistance = 0;
        end = 0;
        start = System.currentTimeMillis();
    }

    public boolean isStarted() {
        return start != 0;
    }

    public boolean isActive() {
        return isStarted() && !isStopped();
    }

    public long getEnd() {
        return end;
    }

    public void stop() {
        end = System.currentTimeMillis();
    }

    public boolean isStopped() {
        return end != 0;
    }

    public float getLastAccuracy() {
        return lastLocation().getAccuracy();
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public String getTotalDistanceString() {
        if (totalDistance >= 1000) {
            return String.format("%d Kilometer %d Meter",
                    (int)totalDistance / 1000,
                    (int)totalDistance % 1000);
        } else {
            return String.format("%d Meter", (int)getTotalDistance());
        }
    }

    public float getLastSpeed() {
        return lastLocation().getSpeed() * 3.6f;
    }

    public String getLastSpeedString() {
        return numberFormat.format(getLastSpeed()) + " km/h";
    }

    // millis per km
    public long getPace() {
        return totalDistance > 0 ? (long)(getDuration()/totalDistance*1000) : 0;
    }

    public String getPaceString() {
        return formatDuration(getPace());
    }


    public String getKilometerPaceString() {
        if (pacePerKilometer.size() == 0) {
            return formatDuration(0);
        } else {
            StringBuilder result = new StringBuilder();
            for (int i = pacePerKilometer.size() -1; i >= 0; i--) {
                result.append(formatDuration(pacePerKilometer.get(i)));
                if (i > 0) {
                    result.append(", ");
                }
            }
            return result.toString();
        }
    }

    // millis
    public long getDuration() {
        if (!isStarted()) return 0;
        if (isStopped()) return end - start;
        return System.currentTimeMillis() - start;
    }

    public String getDurationString() {
       return formatDuration(getDuration());
    }

    private static String formatDuration(long millis) {
        long seconds = millis/1000;
        return String.format(
                "%d:%02d:%02d",
                seconds / 3600,
                (seconds % 3600) / 60,
                seconds % 60);
    }

    private Location lastLocation() {
        return locations.isEmpty() ? new Location("empty") : locations.get(locations.size()-1);
    }

    public void addLocation(Location newLocation) {
        if (isActive() && !locations.isEmpty()) {
            if (beginCurrentKilometer == 0) {
                beginCurrentKilometer = System.currentTimeMillis();
            }
            float newDistance = locations.isEmpty() ? 0 : newLocation.distanceTo(lastLocation());
            if (newDistance > 0) {
                int currentKilometer = (int) (totalDistance / 1000);
                int newKilometer = (int) ((totalDistance + newDistance) / 1000);
                if (newKilometer > currentKilometer) {
                    long durationLastKilometer = System.currentTimeMillis() - beginCurrentKilometer;
                    float distanceCurrentKilometer = totalDistance + newDistance - currentKilometerDistance;
                    pacePerKilometer.add((long)(durationLastKilometer/distanceCurrentKilometer*1000));
                    Toast.makeText(app.getApplicationContext(), formatDuration(durationLastKilometer),
                            Toast.LENGTH_LONG).show();
                    beginCurrentKilometer = System.currentTimeMillis();
                    currentKilometerDistance = totalDistance + newDistance;
                }
                totalDistance += newDistance;
            }
        }
        locations.add(newLocation);
    }

    public List<Location> getLocations() {
        return locations;
    }


}
