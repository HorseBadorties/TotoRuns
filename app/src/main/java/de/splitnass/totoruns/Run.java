package de.splitnass.totoruns;


import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Run {

    private long start, end;
    private List<Location> locations = new ArrayList<>();
    private float totalDistance; // in meters

    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    static {
        numberFormat.setMaximumFractionDigits(2);
    }
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

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

    // minutes per km
    public float getPace() {
        return 0;
    }

    public String getPaceString() {
        return numberFormat.format(getPace()) + " minutes/km";
    }

    // millis
    public long getDuration() {
        if (!isStarted()) return 0;
        if (isStopped()) return end - start;
        return System.currentTimeMillis() - start;
    }

    public String getDurationString() {
        return sdf.format(getDuration());
    }

    private Location lastLocation() {
        return locations.isEmpty() ? new Location("empty") : locations.get(locations.size()-1);
    }

    public void addLocation(Location newLocation) {
        if (isActive() && !locations.isEmpty()) {
            totalDistance += newLocation.distanceTo(lastLocation());
        }
        locations.add(newLocation);
    }

    public List<Location> getLocations() {
        return locations;
    }


}
