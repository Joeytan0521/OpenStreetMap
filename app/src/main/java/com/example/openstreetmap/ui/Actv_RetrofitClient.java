package com.example.openstreetmap.ui;

import com.example.openstreetmap.model.Location;
import com.example.openstreetmap.util.LocationDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Actv_RetrofitClient {
    private static final String BASE_URL = "https://api.openrouteservice.org/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        // Create a logging interceptor to log requests and responses
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Build the OkHttpClient with the interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        // Create a Gson instance with custom configurations
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(
                        new TypeToken<List<Location>>() {}.getType(),
                        new LocationDeserializer())
                .create();

        // Build Retrofit with OkHttpClient and GsonConverterFactory
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
