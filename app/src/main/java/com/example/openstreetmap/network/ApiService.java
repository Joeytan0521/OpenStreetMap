package com.example.openstreetmap.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.example.openstreetmap.model.Directions;
import com.example.openstreetmap.model.Location;

public interface ApiService {

    @GET("geocode/search")
    Call<Location> getGeocodeSearch(
            @Query("api_key") String apiKey,
            @Query("text") String text,
            @Query("sources") String sources,
            @Query("layers") String layers,
            @Query("size") int size,
            @Query("boundary.country") String country
    );

    @GET("v2/directions/driving-car")
    Call<Directions> getDirections(
            @Query("api_key") String apiKey,
            @Query("start") String start,
            @Query("end") String end
    );
}

