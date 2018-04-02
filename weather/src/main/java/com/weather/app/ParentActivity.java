package com.weather.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.model.City;

import java.util.List;


public class ParentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get teh app's shared prefs
        SharedPreferences prefs = getSharedPreferences("app", Context.MODE_PRIVATE);

        // Is this our first time running the app?
        boolean firstRun = prefs.getBoolean("isFirstAppLaunch", true);
        Intent intent;
        if (firstRun) {
            // Load settings
            intent = new Intent(this, InitialAppLaunch.class);
        } else {
            // Load weather
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }




}
