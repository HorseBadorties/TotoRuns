package de.splitnass.totoruns;

import android.app.Application;

public class RunApplication extends Application {

    private Run run;

    @Override
    public void onCreate() {
        super.onCreate();
        run = new Run(this);
    }

    public Run getRun() {
        return run;
    }
}
