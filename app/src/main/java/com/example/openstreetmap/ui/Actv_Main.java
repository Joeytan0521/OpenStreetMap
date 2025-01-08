package com.example.openstreetmap.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.openstreetmap.R;
import com.example.openstreetmap.adapter.Adapter_location;
import com.example.openstreetmap.databinding.ActvMainBinding;
import com.example.openstreetmap.model.Directions;
import com.example.openstreetmap.model.Location;
import com.example.openstreetmap.network.ApiService;
import com.example.openstreetmap.util.LocationPreferences;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Actv_Main extends AppCompatActivity implements MapListener, GpsStatus.Listener {

    private Marker endMarker;
    private Marker startMarker;
    private ApiService apiService;
    private ActvMainBinding binding;
    private IMapController controller;
    private Polyline previousPolyline;
    private MyLocationNewOverlay mMyLocationOverlay;
    private final Handler handler = new Handler();
    private static final int DEBOUNCE_DELAY = 300;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String API_KEY = "5b3ce3597851110001cf62489cdbe9fc2aae49ad8621b47ec5a197be";
    private double startingLatitude, startingLongitude, destinationLatitude, destinationLongitude  = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActvMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = Actv_RetrofitClient.getClient().create(ApiService.class);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            initializeMap();
        }

        TextWatcher startingPointWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String query = charSequence.toString().trim();
                    if (!query.isEmpty()) {
                        fetchGeocodeData(query, "Starting Point");
                    }
                    binding.actvMainRv.setVisibility(View.VISIBLE);
                }, DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };
        binding.actvMainStartingpointEt.addTextChangedListener(startingPointWatcher);
        binding.actvMainStartingpointEt.setTag(R.id.text_watcher_tag, startingPointWatcher);

        TextWatcher destinationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String query = charSequence.toString().trim();
                    if (!query.isEmpty()) {
                        fetchGeocodeData(query, "Destination");
                    }
                    binding.actvMainRv.setVisibility(View.VISIBLE);
                }, DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };
        binding.actvMainDestinationEt.addTextChangedListener(destinationWatcher);
        binding.actvMainDestinationEt.setTag(R.id.text_watcher_tag, destinationWatcher);

        binding.actvMainStartingpointEt.setOnFocusChangeListener((v, hasFocus) -> handleFocusChange(hasFocus, false));
        binding.actvMainDestinationEt.setOnFocusChangeListener((v, hasFocus) -> handleFocusChange(hasFocus, true));

        binding.actvMainSearchBtn.setOnClickListener(v -> {
            binding.actvMainRv.setVisibility(View.GONE);
            if (mMyLocationOverlay != null) {
                mMyLocationOverlay.disableFollowLocation();
            }
            if (startingLatitude != 0.0 && startingLongitude != 0.0 && destinationLatitude != 0.0 && destinationLongitude != 0.0) {
                fetchDirections(startingLatitude, startingLongitude, destinationLatitude, destinationLongitude);
            } else {
                Toast.makeText(this, "Please select both starting point and destination.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.actvMainFavBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Actv_Main.this, Actv_Fav_List.class);
            startActivity(intent);
        });

        binding.actvMainGpsBtn.setOnClickListener(v -> {
            if (mMyLocationOverlay != null && mMyLocationOverlay.getMyLocation() != null) {
                GeoPoint myLocation = mMyLocationOverlay.getMyLocation();
                if (myLocation != null) {
                    mMyLocationOverlay.enableFollowLocation();
                    zoomToRoute(myLocation.getLatitude(), myLocation.getLongitude());
                    Toast.makeText(this, "You are here!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Unable to get your location. Make sure GPS is enabled.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location tracking not enabled.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGeocodeData(String location, String type) {
        apiService.getGeocodeSearch(API_KEY, location, "openstreetmap", "venue", 10, "MY")
                .enqueue(new Callback<Location>() {
                    @Override
                    public void onResponse(@NonNull Call<Location> call, @NonNull Response<Location> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Location.LocationFeature> locationFeatures = response.body().getFeatures();
                            displayGeocodeResults(locationFeatures, type);
                        } else {
                            Log.e("GeocodeResponse", "Response was unsuccessful or empty.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Location> call, @NonNull Throwable t) {
                        Log.e("Geocode", "API call failed: " + t.getMessage());
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayGeocodeResults(List<Location.LocationFeature> locationFeatures, String type) {
        Adapter_location adapter = new Adapter_location(locationFeatures, feature -> {
            String name = feature.getProperties() != null ? feature.getProperties().getName() : "Unknown Location";

            if (type.equals("Starting Point")) {
                updateEditTextAndHideRecyclerView(
                        binding.actvMainStartingpointEt,
                        name,
                        feature.getGeometry().getLatitude(),
                        feature.getGeometry().getLongitude(),
                        "Starting Point",
                        true
                );
            } else {
                updateEditTextAndHideRecyclerView(
                        binding.actvMainDestinationEt,
                        name,
                        feature.getGeometry().getLatitude(),
                        feature.getGeometry().getLongitude(),
                        "Destination",
                        false
                );
            }
        });

        binding.actvMainRv.setLayoutManager(new LinearLayoutManager(this));
        binding.actvMainRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void updateEditTextAndHideRecyclerView(EditText editText, String text, double latitude, double longitude, String markerTitle, boolean isStartingPoint) {
        Log.d("LogLocation", "Selected " + (isStartingPoint ? "Starting Point" : "Destination") + ": " + text + " (" + latitude + ", " + longitude + ")");

        Location.LocationFeature locationFeature = createLocationFeature(text, latitude, longitude);

        if (isStartingPoint) {
            startingLatitude = latitude;
            startingLongitude = longitude;
            LocationPreferences.saveLocationForStartingPoint(this, locationFeature, true);
        } else {
            destinationLatitude = latitude;
            destinationLongitude = longitude;
            LocationPreferences.saveLocationForDestination(this, locationFeature, true);
        }

        TextWatcher watcher = editText.getTag(R.id.text_watcher_tag) != null
                ? (TextWatcher) editText.getTag(R.id.text_watcher_tag)
                : null;
        if (watcher != null) {
            editText.removeTextChangedListener(watcher);
        }

        editText.setText(text);
        editText.setSelection(text.length());

        addMarker(latitude, longitude, markerTitle, isStartingPoint);

        toggleRecyclerView(false);
        hideKeyboard();

        if (watcher != null) {
            editText.addTextChangedListener(watcher);
        }
    }

    private Location.LocationFeature createLocationFeature(String text, double latitude, double longitude) {
        Location.LocationFeature locationFeature = new Location.LocationFeature();
        Location.LocationFeature.Properties properties = new Location.LocationFeature.Properties();
        properties.setName(text);
        locationFeature.setProperties(properties);

        Location.LocationFeature.Geometry geometry = new Location.LocationFeature.Geometry();
        geometry.setCoordinates(new double[]{longitude, latitude});
        locationFeature.setGeometry(geometry);

        return locationFeature;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    private void toggleRecyclerView(boolean isVisible) {
        binding.actvMainRv.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void fetchDirections(double startLat, double startLon, double destLat, double destLon) {
        String start = startLon + ", " + startLat;
        String end = destLon + ", " + destLat;

        apiService.getDirections(API_KEY, start, end)
                .enqueue(new Callback<Directions>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(@NonNull Call<Directions> call, @NonNull Response<Directions> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Directions directions = response.body();
                            if (directions.getFeatures() != null && !directions.getFeatures().isEmpty()) {
                                Directions.Feature feature = directions.getFeatures().get(0);

                                if (feature.getGeometry() != null) {
                                    List<List<Double>> routeCoordinates = feature.getGeometry().getCoordinates();
                                    drawRouteOnMap(routeCoordinates);
                                    zoomToRoute(routeCoordinates);
                                }

                                if (feature.getProperties() != null && !feature.getProperties().getSegments().isEmpty()) {
                                    Directions.Feature.Properties.Segment segment = feature.getProperties().getSegments().get(0);
                                    double distance = segment.getDistance();
                                    double duration = segment.getDuration();

                                    @SuppressLint("DefaultLocale") String distanceText = String.format("%.2f km", distance / 1000);
                                    String durationText = formatDuration(duration);

                                    binding.actvMainDistanceTv.setText("Distance: " + distanceText);
                                    binding.actvMainDurationTv.setText("Duration: " + durationText);

                                    long currentTimeMillis = System.currentTimeMillis();
                                    long arrivalTimeMillis = currentTimeMillis + TimeUnit.SECONDS.toMillis((long) duration);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                                    Date arrivalDate = new Date(arrivalTimeMillis);
                                    String formattedArrivalTime = sdf.format(arrivalDate);
                                    binding.actvMainETATv.setText("ETA: " + formattedArrivalTime);
                                }
                            } else {
                                Log.e("RouteAPI", "No features found in the response.");
                            }
                        } else {
                            Log.e("RouteAPI", "Failed to retrieve route. Response was unsuccessful or empty.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Directions> call, @NonNull Throwable t) {
                        Toast.makeText(Actv_Main.this, "Failed to fetch directions. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e("RouteAPI", "API call failed: " + t.getMessage());
                    }
                });
    }

    @SuppressLint("DefaultLocale")
    private String formatDuration(double seconds) {
        int hours = (int) seconds / 3600;
        int minutes = (int) (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%d hour%s %d min%s", hours, (hours > 1 ? "s" : ""), minutes, (minutes != 1 ? "s" : ""));
        } else {
            return String.format("%d min%s", minutes, (minutes != 1 ? "s" : ""));
        }
    }

    private void drawRouteOnMap(List<List<Double>> coordinates) {
        // Remove the previous polyline if it exists
        if (previousPolyline != null) {
            MapView mMap = binding.actvMainMapview;
            mMap.getOverlayManager().remove(previousPolyline);
        }

        List<GeoPoint> geoPoints = new ArrayList<>();
        for (List<Double> coordinate : coordinates) {
            double longitude = coordinate.get(0);
            double latitude = coordinate.get(1);
            geoPoints.add(new GeoPoint(latitude, longitude));
        }

        Polyline polyline = new Polyline();
        polyline.setPoints(geoPoints);
        polyline.setWidth(8f);
        polyline.setColor(getResources().getColor(R.color.route_color, null));

        MapView mMap = binding.actvMainMapview;
        mMap.getOverlayManager().add(polyline);

        // Store the new polyline reference for future removal
        previousPolyline = polyline;

        mMap.invalidate();
    }

    private void addMarker(double latitude, double longitude, String title, boolean isStart) {
        // Remove the previous markers if they exist
        if (isStart && startMarker != null) {
            binding.actvMainMapview.getOverlays().remove(startMarker);
        }
        if (!isStart && endMarker != null) {
            binding.actvMainMapview.getOverlays().remove(endMarker);
        }

        Marker marker = new Marker(binding.actvMainMapview);
        marker.setPosition(new GeoPoint(latitude, longitude));
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        @SuppressLint("UseCompatLoadingForDrawables") Drawable markerIcon = getResources().getDrawable(R.drawable.ic_location, null);

        if (isStart) {
            markerIcon.setColorFilter(ContextCompat.getColor(this, R.color.starting_point_yellow), PorterDuff.Mode.SRC_IN);
        } else {
            markerIcon.setColorFilter(ContextCompat.getColor(this, R.color.destination_red), PorterDuff.Mode.SRC_IN);
        }

        marker.setIcon(markerIcon);

        binding.actvMainMapview.getOverlays().add(marker);
        binding.actvMainMapview.invalidate();

        // Store the marker reference for future removal
        if (isStart) {
            startMarker = marker;
        } else {
            endMarker = marker;
        }
    }

    private void zoomToRoute(List<List<Double>> routeCoordinates) {
        if (routeCoordinates == null || routeCoordinates.isEmpty()) return;

        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;

        for (List<Double> coord : routeCoordinates) {
            double lat = coord.get(1);
            double lon = coord.get(0);

            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
            if (lon < minLon) minLon = lon;
            if (lon > maxLon) maxLon = lon;
        }

        double padding = 0.005;
        minLat -= padding;
        maxLat += padding;
        minLon -= padding;
        maxLon += padding;

        BoundingBox boundingBox = new BoundingBox(maxLat, maxLon, minLat, minLon);

        binding.actvMainMapview.zoomToBoundingBox(boundingBox, true);
    }

    private void zoomToRoute(double latitude, double longitude) {
        double padding = 0.005;
        BoundingBox boundingBox = new BoundingBox(
                latitude + padding,
                longitude + padding,
                latitude - padding,
                longitude - padding
        );

        binding.actvMainMapview.zoomToBoundingBox(boundingBox, true);
    }

    private void handleFocusChange(boolean hasFocus, boolean isDestination) {
        if (hasFocus) {
            List<Location.LocationFeature> locations = new ArrayList<>();

            List<Location.LocationFeature> previousLocations = isDestination
                    ? LocationPreferences.getRecentLocationsForDestination(this)
                    : LocationPreferences.getRecentLocations(this);

            if (mMyLocationOverlay != null && mMyLocationOverlay.getMyLocation() != null) {
                GeoPoint myLocation = mMyLocationOverlay.getMyLocation();

                if (myLocation != null) {
                    Location.LocationFeature currentLocation = new Location.LocationFeature();
                    Location.LocationFeature.Properties properties = new Location.LocationFeature.Properties();
                    properties.setName("Current Location");

                    String region = getRegionFromCoordinates(myLocation.getLatitude(), myLocation.getLongitude());
                    properties.setRegion(region);

                    currentLocation.setProperties(properties);

                    Location.LocationFeature.Geometry geometry = new Location.LocationFeature.Geometry();
                    geometry.setCoordinates(new double[]{myLocation.getLongitude(), myLocation.getLatitude()});
                    currentLocation.setGeometry(geometry);

                    locations.add(0, currentLocation);
                } else {
                    Toast.makeText(this, "Unable to get your location. Make sure GPS is enabled.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location tracking not enabled.", Toast.LENGTH_SHORT).show();
            }

            for (Location.LocationFeature location : previousLocations) {
                String region = getRegionFromCoordinates(location.getGeometry().getLatitude(), location.getGeometry().getLongitude());
                location.getProperties().setRegion(region);
                boolean isDuplicate = false;

                for (Location.LocationFeature item : locations) {
                    if (item.getProperties().getName().equals(location.getProperties().getName())) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    locations.add(location);
                }
            }


            Adapter_location adapter = new Adapter_location(locations, feature -> updateEditTextAndHideRecyclerView(
                    isDestination ? binding.actvMainDestinationEt : binding.actvMainStartingpointEt,
                    feature.getProperties().getName(),
                    feature.getGeometry().getLatitude(),
                    feature.getGeometry().getLongitude(),
                    isDestination ? "Destination" : "Starting Point",
                    !isDestination
            ));

            binding.actvMainRv.setLayoutManager(new LinearLayoutManager(this));
            binding.actvMainRv.setAdapter(adapter);
            binding.actvMainRv.setVisibility(View.VISIBLE);

        } else {
            binding.actvMainRv.setVisibility(View.GONE);
        }
    }

    private String getRegionFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String region = address.getAdminArea();
                return region != null ? region : "Unknown Region";
            } else {
                return "Unknown Region";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown Region";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            }
        }
    }

    private void initializeMap() {
        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        );

        // Initialize MapView and MyLocationNewOverlay
        MapView mMap = binding.actvMainMapview;
        mMap.setTileSource(TileSourceFactory.MAPNIK);
        mMap.setMultiTouchControls(true);

        mMyLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mMap);
        controller = mMap.getController();

        // Enable MyLocation overlay and Follow location
        mMyLocationOverlay.enableMyLocation();
        mMyLocationOverlay.enableFollowLocation();
        mMyLocationOverlay.setDrawAccuracyEnabled(true);

        mMyLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            controller.setCenter(mMyLocationOverlay.getMyLocation());
            controller.animateTo(mMyLocationOverlay.getMyLocation());
        }));

        controller.setZoom(12.0);
        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.addMapListener(this);
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        if (event != null && event.getSource() != null) {
            Log.e("TAG", "onScroll: la " + event.getSource().getMapCenter().getLatitude());
            Log.e("TAG", "onScroll: lo " + event.getSource().getMapCenter().getLongitude());
        }
        return true;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        if (event != null) {
            Log.e("TAG", "onZoom zoom level: " + event.getZoomLevel() + " source: " + event.getSource());
        }
        return false;
    }

    @Override
    public void onGpsStatusChanged(int event) {
    }

}

