package com.weather.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import at.markushi.ui.CircleButton;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault;
import com.survivingwithandroid.weather.lib.exception.LocationProviderNotFoundException;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;

import java.util.ArrayList;
import java.util.List;


public class InitialAppLaunch extends ActionBarActivity implements
        View.OnClickListener,
        Toolbar.OnMenuItemClickListener,
        View.OnFocusChangeListener {


    private TextView userMessage;
    private ProgressWheel progWheel;
    private MyRecyclerAdapter adapter;
    private WeatherClient.ClientBuilder builder;
    private WeatherConfig config;
    private CircleButton cb;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_app_launch);

        prefs = getSharedPreferences("app", Context.MODE_PRIVATE);

        userMessage = (TextView) findViewById(R.id.notify);
        progWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        cb = (CircleButton) findViewById(R.id.okay);
        cb.setEnabled(false);




        builder = new WeatherClient.ClientBuilder();

        config = new WeatherConfig();
        config.unitSystem = WeatherConfig.UNIT_SYSTEM.M;
        config.lang = "en"; // If you want to use english
        config.maxResult = 5; // Max number of cities retrieved
        config.numDays = 6; // Max num of days in the forecast

        createList();

        searchCity();

    }

    // Creates the list view used for
    // displaying to the user
    private void createList() {
        List<City> list = new ArrayList<>();
        final RecyclerView recyclerView =
                (RecyclerView) findViewById(R.id.city_list);
        recyclerView.setHasFixedSize(true);
        adapter =
                new MyRecyclerAdapter(this, list, R.layout.city_item);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this);
        linearLayoutManager
                .setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    // Searches for city using gps/network
    private void searchCity() {
        toggleProgress();
        try {
            WeatherClient client = builder.attach(this)
                    .provider(new OpenweathermapProviderType())
                    .httpClient(WeatherClientDefault.class)
                    .config(config)
                    .build();

            // Try to find a good location
            // using medium settings
            Criteria criteria = new Criteria();
            criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
            criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
            // Can we use data?
            criteria.setCostAllowed(true);

            // Search city by gps/network using
            // above critera
            client.searchCityByLocation(
                    criteria,
                    new WeatherClient.CityEventListener() {

                        // When we get the city list
                        @Override
                        public void onCityListRetrieved(List<City> cityList) {
                            for (int i = 0; i < cityList.size(); i++) {
                                adapter.set(cityList.get(i), i);
                            }
                            displayMessage("Not the correct results?" +
                                    " Press the button above to try again.");
                        }


                        @Override
                        public void onWeatherError(WeatherLibException wle) {
                            displayMessage("There seems to be no " +
                                    "weather data, please try again later.");

                        }


                        @Override
                        public void onConnectionError(Throwable t) {
                            displayMessage("Whoops! We can't seem to " +
                                    "connect to the weather servers.");
                        }

                    });

        } catch (WeatherProviderInstantiationException e) {
            displayMessage("Error: Unable to access " +
                    "the weather provider.");
        } catch (LocationProviderNotFoundException e) {
            displayMessage("Whoops! Unable to access " +
                    "location provider.");
        } catch (NullPointerException e) {
            displayMessage("Whoops! We can't seem to" +
                    "connect to the weather servers.");
        }

    }

    // Sets a user message
    private void displayMessage(String text) {
        toggleProgress();
        userMessage.setVisibility(View.VISIBLE);
        userMessage.setText(text);
    }

    // Toggle the spin wheel if active
    private void toggleProgress() {
        if (progWheel.getVisibility() == View.VISIBLE) {
            // Needs its own thread for timing control
            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    if (progWheel.getVisibility()
                            == View.VISIBLE) {
                        progWheel.setVisibility(View.GONE);
                    }
                }
            };
            handler.postDelayed(r, 1);
        } else if (progWheel.getVisibility() == View.GONE) {
            progWheel.spin();
            progWheel.setVisibility(View.VISIBLE);
        }
    }

    // Make sure to not show this activity ever again
    // on startup!
    private void dontShowThisAgain() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFirstAppLaunch", false);
        editor.apply();
    }



    // When we click certain things...
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            // When we click the gps mini button...
            case R.id.fab:
                searchCity();
                break;
            // Click the okay button
            case R.id.okay:
                dontShowThisAgain();
                // If you started this act as normal
                // i.e startActivity then this will
                // return null. If the user runs this on
                // inital app launch this will return null
                if (getCallingActivity() == null) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else {
                    setResult(RESULT_OK);
                    finish();
                }

                break;
            default:
                break;
        }
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    // If and when we click a list view item
    // make sure we actually enable the "okay"
    // action button
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.city_item_click && hasFocus) {

            cb.setColor(getResources()
                    .getColor(R.color.primary_orange));
            cb.setEnabled(true);
            cb.setPressed(true);
            // Fancy flash animation
            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    if (cb != null)
                        cb.setPressed(false);
                }
            };
            handler.postDelayed(r, 200);
            // We can now click it!
            cb.setClickable(true);
        }
    }
}
