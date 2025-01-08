package com.example.openstreetmap.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.openstreetmap.model.Location;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationPreferences {

    private static final String PREF_NAME = "location_preferences";
    private static final String KEY_START_LOCATIONS = "recent_start_locations";
    private static final String KEY_DESTINATION_LOCATIONS = "recent_destination_locations";
    private static final String KEY_FAV_LOCATIONS = "favorite_locations";

    private static void saveLocation(Context context, String key, Location.LocationFeature locationFeature, boolean isPreviouslySelected) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        List<Location.LocationFeature> locations = getLocations(context, key);

        locationFeature.setPreviouslySelected(isPreviouslySelected);

        if ("Current Location".equals(locationFeature.getProperties().getName())) {
            locations.removeIf(location -> "Current Location".equals(location.getProperties().getName()));
            locations.add(0, locationFeature);
        } else {
            if (!locations.contains(locationFeature)) {
                locations.add(0, locationFeature);
            }
        }

        // Limit the list to a maximum of 5 locations
        if (locations.size() > 5) {
            locations = locations.subList(0, 5);
        }

        Gson gson = new Gson();
        String json = gson.toJson(locations);
        editor.putString(key, json);
        editor.apply();
    }

    public static void saveLocationForStartingPoint(Context context, Location.LocationFeature locationFeature, boolean isPreviouslySelected) {
        saveLocation(context, KEY_START_LOCATIONS, locationFeature, isPreviouslySelected);
    }

    public static void saveLocationForDestination(Context context, Location.LocationFeature locationFeature, boolean isPreviouslySelected) {
        saveLocation(context, KEY_DESTINATION_LOCATIONS, locationFeature, isPreviouslySelected);
    }

    private static List<Location.LocationFeature> getLocations(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(key, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Location.LocationFeature>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<Location.LocationFeature> getRecentLocations(Context context) {
        return getLocations(context, KEY_START_LOCATIONS);
    }

    public static List<Location.LocationFeature> getRecentLocationsForDestination(Context context) {
        return getLocations(context, KEY_DESTINATION_LOCATIONS);
    }

    public static void clearLocations(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(KEY_START_LOCATIONS).apply();
        sharedPreferences.edit().remove(KEY_DESTINATION_LOCATIONS).apply();
    }
}
