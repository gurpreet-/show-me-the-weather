package com.weather.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherCode;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.City;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity
        implements Toolbar.OnMenuItemClickListener {

    public static final int SETTINGS = 20;
    private SharedPreferences prefs;
    private TextView where;
    private TextView temp;
    private TextView press;
    private TextView windSpeed;
    private TextView tempMin;
    private TextView tempMax;
    private TextView sunrise;
    private TextView sunset;
    private ImageView imgView;
    private WeatherClient.ClientBuilder builder;
    private WeatherConfig config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("app", Context.MODE_PRIVATE);
        where = (TextView) findViewById(R.id.where);

        temp = (TextView) findViewById(R.id.temp);
        imgView = (ImageView) findViewById(R.id.img_weather);
        press = (TextView) findViewById(R.id.pressure);
        windSpeed = (TextView) findViewById(R.id.wind_speed);
        tempMin = (TextView) findViewById(R.id.temp_min);
        tempMax = (TextView) findViewById(R.id.temp_max);
        sunrise = (TextView) findViewById(R.id.sunrise);
        sunset = (TextView) findViewById(R.id.sunset);


        // Set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set an OnMenuItemClickListener to handle menu item clicks
        toolbar.setOnMenuItemClickListener(this);
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.main);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        builder = new WeatherClient.ClientBuilder();

        config = new WeatherConfig();
        config.unitSystem = WeatherConfig.UNIT_SYSTEM.M;
        config.lang = "en"; // If you want to use english
        config.maxResult = 5; // Max number of cities retrieved
        config.numDays = 6; // Max num of days in the forecast


        displayCurrentWeather();


    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (requestCode == SETTINGS) {
            if (resultCode == RESULT_OK) {
                displayCurrentWeather();
            }
            if (resultCode == RESULT_CANCELED) {
                // Do nothing
            }
        }
    }



    public static String convertDate(long unixTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(unixTime * 1000);
        sdf.setTimeZone(cal.getTimeZone());
        return sdf.format(cal.getTime());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action
        // bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, InitialAppLaunch.class);
            startActivityForResult(intent, SETTINGS);
        }
        else if (id == R.id.action_refresh) {
            where.setText("Loading...");
            temp.setText("Loading...");
            displayCurrentWeather();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return false;
    }

    // Gets the current weather and displays it
    public void displayCurrentWeather() {
        // Get the cityname and ID
        String cityName = prefs.getString("cityName", null);
        String cityID = prefs.getString("cityID", null);
        City city = buildCity(cityName, cityID);

        try {
            // Create a weather client
            final WeatherClient client = builder.attach(this)
                    .provider(new OpenweathermapProviderType())
                    .httpClient(WeatherClientDefault.class)
                    .config(config)
                    .build();

            // Get the clients current condition
            client.getCurrentCondition(
                    new WeatherRequest(city.getId()),
                    new WeatherClient.WeatherEventListener() {
                        @Override
                        public void onWeatherRetrieved(CurrentWeather currentWeather) {
                            // Now set some shorthand access variables
                            Weather weather = currentWeather.weather;
                            Weather.Condition condition = weather.currentCondition;
                            String wUnit = currentWeather.getUnit().tempUnit;
                            String wSpeedUnit = currentWeather.getUnit().speedUnit;
                            String pUnit = currentWeather.getUnit().pressureUnit;
                            where.setText(weather.location.getCity());

                            // Set the image
                            int code =
                                    getWeatherImage(condition.getWeatherCode());
                            Drawable weatherImage = getResources().getDrawable(code);
                            imgView.setImageDrawable(weatherImage);

                            // Set the rest of the views
                            temp.setText("" + ((int) weather.temperature.getTemp()) + wUnit);
                            tempMin.setText(weather.temperature.getMinTemp() + wUnit);
                            tempMax.setText(weather.temperature.getMaxTemp() + wUnit);
                            windSpeed.setText(weather.wind.getSpeed() + wSpeedUnit);
                            press.setText(condition.getPressure() + pUnit);
                            sunrise.setText(convertDate(weather.location.getSunrise()));
                            sunset.setText(convertDate(weather.location.getSunset()));
                        }

                        @Override
                        public void onWeatherError(WeatherLibException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onConnectionError(Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });

        } catch (WeatherProviderInstantiationException e) {
            e.printStackTrace();
        }
    }


    // Get the weather image using the
    // WeatherCode that we're given
    private int getWeatherImage(WeatherCode code) {
        switch (code) {
            case BLOWING_SNOW:
                return R.drawable.snow4;
            case BLUSTERY:
                return R.drawable.shower1;
            case CLEAR_NIGHT:
                return R.drawable.sunny_night;
            case CLOUDY:
                return R.drawable.cloudy1;
            case COLD:
                return R.drawable.cloudy5;
            case DRIZZLE:
                return R.drawable.shower1;
            case DUST:
                return R.drawable.mist;
            case FAIR_DAY:
                return R.drawable.sunny;
            case FAIR_NIGHT:
                return R.drawable.sunny_night;
            case FOGGY:
                return R.drawable.fog;
            case FREEZING_DRIZZLE:
                return R.drawable.snow2;
            case FREEZING_RAIN:
                return R.drawable.sleet;
            case HAIL:
                return R.drawable.hail;
            case HAZE:
                return R.drawable.fog;
            case HEAVY_SHOWERS:
                return R.drawable.shower3;
            case HEAVY_SNOW:
                return R.drawable.snow5;
            case HOT:
                return R.drawable.sunny;
            case HURRICANE:
                return R.drawable.tstorm3;
            case ISOLATED_THUDERSHOWERS:
                return R.drawable.tstorm2;
            case ISOLATED_THUNDERSTORMS:
                return R.drawable.tstorm2;
            case LIGHT_SNOW_SHOWERS:
                return R.drawable.snow2;
            case MIXED_RAIN_AND_HAIL:
                return R.drawable.hail;
            case MIXED_RAIN_SLEET:
                return R.drawable.sleet;
            case MIXED_RAIN_SNOW:
                return R.drawable.sleet;
            case MOSTLY_CLOUDY_DAY:
                return R.drawable.cloudy3;
            case MOSTLY_CLOUDY_NIGHT:
                return R.drawable.cloudy3_night;
            case NOT_AVAILABLE:
                return R.drawable.dunno;
            case PARTLY_CLOUD:
                return R.drawable.cloudy2;
            case PARTLY_CLOUDY_DAY:
                return R.drawable.cloudy1;
            case PARTLY_CLOUDY_NIGHT:
                return R.drawable.cloudy1_night;
            case SCATTERED_SHOWERS:
                return R.drawable.light_rain;
            case SCATTERED_SNOW_SHOWERS:
                return R.drawable.snow1;
            case SCATTERED_THUNDERSTORMS:
                return R.drawable.tstorm2;
            case SCATTERED_THUNDERSTORMS_1:
                return R.drawable.tstorm2;
            case SEVERE_THUNDERSTORMS:
                return R.drawable.tstorm3;
            case SHOWERS:
                return R.drawable.shower2;
            case SLEET:
                return R.drawable.sleet;
            case SMOKY:
                return R.drawable.mist;
            case SNOW:
                return R.drawable.snow3;
            case SNOW_FLURRIES:
                return R.drawable.snow2;
            case SNOW_SHOWERS:
                return R.drawable.snow4;
            case SUNNY:
                return R.drawable.sunny;
            case THUNDERSHOWERS:
                return R.drawable.tstorm2;
            case THUNDERSTORMS:
                return R.drawable.tstorm2;
            case TORNADO:
                return R.drawable.tstorm3;
            case TROPICAL_STORM:
                return R.drawable.tstorm3;
            case WINDY:
                return R.drawable.overcast;
            default:
                return R.drawable.dunno;
        }
    }

    // Create a city object using just the city
    // name and its ID
    private City buildCity(String name, String id) {
        if (id == null) {
            throw new IllegalArgumentException("Null string");
        }
        City.CityBuilder builder = new City.CityBuilder();
        builder.id(id);
        builder.name(name);
        return builder.build();
    }
}
