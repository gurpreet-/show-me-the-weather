package com.weather.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.survivingwithandroid.weather.lib.model.City;

import java.io.IOException;
import java.util.List;

public class MyRecyclerAdapter extends
        RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private static Activity context;
    private static List<City> items;
    private int itemLayout;
    private Double currentLat;
    private Double currentLong;

    public MyRecyclerAdapter(Activity context,
                             final List<City> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
        this.context = context;

        // Get the location and store
        // the long and lat in fields
        MyLocation.LocationResult locationResult
                = new MyLocation.LocationResult(){

            @Override
            public void gotLocation(android.location.Location location) {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();
                }
            }
        };

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(context, locationResult);
    }

    // On creation of view holder..
    @Override public ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(itemLayout, parent, false);
        return new ViewHolder(v);
    }

    // When the view holder is bound..
    @Override public void onBindViewHolder(
            final ViewHolder holder, int position) {
        final City city = items.get(position);
        holder.cityName.setText(city.getName());
        displayDistances(city, holder.distance);
    }


    // Gets the distance between two
    // locations and shows it on screen
    private void displayDistances(
            final City city, final TextView distance) {

        if (currentLat != null && currentLong != null) {
            Geocoder geocoder = new Geocoder(context);
            List<Address> addresses;
            try {
                // Try and get a list of locations
                addresses = geocoder.getFromLocationName(city.getName(), 1);
                if (addresses.size() > 0) {
                    double latitude = addresses.get(0).getLatitude();
                    double longitude = addresses.get(0).getLongitude();
                    float[] result = new float[1];
                    // Then work out the distance between them
                    android.location.Location
                            .distanceBetween(
                                    currentLat,
                                    currentLong,
                                    latitude,
                                    longitude,
                                    result);
                    // result[0] stores the distance
                    distance.setText(Math.round(result[0] / 1000) + " km");
                }

            } catch (IOException e) {
                distance.setText("Unknown");
            }
        } else {
            distance.setText("");
        }


    }

    @Override public int getItemCount() {
        return items.size();
    }

    // Adds an item to position
    public void add(City item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    // Replaces item at pos
    public void set(City item, int position) {
        if (items.size() > position) {
            items.set(position, item);
            notifyItemChanged(position);
        } else {
            add(item, position);
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnTouchListener {
        public TextView cityName;
        public TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            cityName = (TextView) itemView.findViewById(R.id.city);
            distance = (TextView) itemView.findViewById(R.id.city_region);
            itemView.setOnTouchListener(this);
            itemView.setOnFocusChangeListener((InitialAppLaunch) context);
        }


        // When we save the city, save it to
        // shared prefs
        private void saveSelectedCity(String cityName, String id) {
            if (cityName != null) {
                SharedPreferences prefs =
                        context.getSharedPreferences("app", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("cityName", cityName);
                editor.putString("cityID", id);
                editor.apply();
            }
        }

        // Get the city at certian list
        // view click position
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (MotionEvent.ACTION_UP == event.getAction()) {
                City gotACity = items.get(getPosition());
                saveSelectedCity(gotACity.getName(), gotACity.getId());
            }
            return false;
        }
    }




}