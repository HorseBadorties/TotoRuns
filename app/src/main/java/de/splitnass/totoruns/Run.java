package de.splitnass.totoruns;


import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Run {

    private long start, end;
    private List<Location> locations = new ArrayList<>();
    private float totalDistance; // in meters
    private long beginCurrentKilometer, durationLastKilometer;

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

    // minutes per km
    public float getPace() {
        return totalDistance > 0 ? getDuration()/60/totalDistance : 0;
    }

    public String getPaceString() {
        return paceFormatter.format(getPace()*60*1000) + " Min/km";
    }

    // minutes per km
    public float getKilometerPace() {
      return durationLastKilometer > 0 ? durationLastKilometer/60/1000 : 0;
    }

    public String getKilometerPaceString() {
        return paceFormatter.format(getKilometerPace()*60*1000) + " Min/km";
    }

    // millis
    public long getDuration() {
        if (!isStarted()) return 0;
        if (isStopped()) return end - start;
        return System.currentTimeMillis() - start;
    }

    public String getDurationString() {
        long seconds = getDuration()/1000;
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
            int currentKilometer = (int)(totalDistance/1000);
            int newKilometer = (int)((totalDistance+newDistance)/1000);
            if (newKilometer > currentKilometer) {
                durationLastKilometer = System.currentTimeMillis() - beginCurrentKilometer;
                beginCurrentKilometer = System.currentTimeMillis();
            }
            totalDistance += newDistance;
        }
        locations.add(newLocation);
    }

    public List<Location> getLocations() {
        return locations;
    }


}
