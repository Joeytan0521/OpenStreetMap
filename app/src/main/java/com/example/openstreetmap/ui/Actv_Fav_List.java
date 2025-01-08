package com.example.openstreetmap.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.openstreetmap.adapter.Adapter_fav_location;
import com.example.openstreetmap.adapter.Adapter_location;
import com.example.openstreetmap.databinding.ActvFavListBinding;
import com.example.openstreetmap.databinding.AlertDialogFavListBinding;
import com.example.openstreetmap.model.FavoriteLocation;
import com.example.openstreetmap.model.Location;
import com.example.openstreetmap.network.ApiService;
import com.example.openstreetmap.network.OnLocationSelectedListener;
import com.example.openstreetmap.util.LocationPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Actv_Fav_List extends AppCompatActivity implements OnLocationSelectedListener {

    private static final int DEBOUNCE_DELAY = 300;
    private static final String API_KEY = "5b3ce3597851110001cf62489cdbe9fc2aae49ad8621b47ec5a197be";

    private Adapter_location adapter;
    private ApiService apiService;
    private ActvFavListBinding binding;
    private AlertDialogFavListBinding dialogBinding;
    private final Handler handler = new Handler();

    private Adapter_fav_location adapter_fav_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActvFavListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = Actv_RetrofitClient.getClient().create(ApiService.class);

        setupRecyclerView();
        setupFloatingActionButton();
        setupHomeandWorkAddress();
        setupAdapter();
    }

    private void setupAdapter() {
        adapter = new Adapter_location(new ArrayList<>(), feature -> {
            String name = feature.getProperties() != null ? feature.getProperties().getName() : "Unknown Location";
            updateEditTextAndHideRecyclerView(
                    dialogBinding.alertDialogFavListEt,
                    name,
                    feature.getGeometry().getLatitude(),
                    feature.getGeometry().getLongitude()
            );
        });
    }

    private void setupFloatingActionButton() {
        binding.actvFavListFab.setOnClickListener(v -> showAddLocationDialog());
    }

    private void showAddLocationDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBinding = AlertDialogFavListBinding.inflate(getLayoutInflater());
        View dialogView = dialogBinding.getRoot();
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();

        dialogBinding.alertDialogFavListRv.setLayoutManager(new LinearLayoutManager(this));
        dialogBinding.alertDialogFavListRv.setAdapter(adapter);

        setupSearchField();
        setupDialogButtons(alertDialog);

        adapter.setOnLocationSelectedListener((locationName, latitude, longitude) -> {
            // Notify the activity about the selected location
            onLocationSelected(locationName, latitude, longitude);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void setupSearchField() {
        dialogBinding.alertDialogFavListEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    String query = charSequence.toString().trim();
                    if (!query.isEmpty()) {
                        fetchGeocodeData(query);
                    }
                    dialogBinding.alertDialogFavListRv.setVisibility(View.VISIBLE);
                }, DEBOUNCE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setupHomeandWorkAddress() {
        binding.actvFavListHomeTv.setOnClickListener(v -> showAddLocationDialog());
        binding.actvFavListWorkTv.setOnClickListener(v -> showAddLocationDialog());
    }

    private void setupDialogButtons(AlertDialog alertDialog) {
        dialogBinding.alertDialogFavListCancelBtn.setOnClickListener(view -> alertDialog.dismiss());
        dialogBinding.alertDialogFavListAddBtn.setOnClickListener(view -> {
            String locationName = Objects.requireNonNull(dialogBinding.alertDialogFavListEt.getText()).toString();
            String region = "HUhuHEhe";

            if (!locationName.isEmpty()) {
                adapter_fav_location.addLocation(new FavoriteLocation(locationName, region));
                alertDialog.dismiss();
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGeocodeData(String location) {
        apiService.getGeocodeSearch(API_KEY, location, "openstreetmap", "venue", 5, "MY")
                .enqueue(new Callback<Location>() {
                    @Override
                    public void onResponse(@NonNull Call<Location> call, @NonNull Response<Location> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Location.LocationFeature> locationFeatures = response.body().getFeatures();
                            adapter.updateData(locationFeatures);
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

    private void updateEditTextAndHideRecyclerView(EditText editText, String text, double latitude, double longitude) {
        Log.d("LogLocation", "Selected: " + text + " (" + latitude + ", " + longitude + ")");
        Location.LocationFeature locationFeature = createLocationFeature(text, latitude, longitude);
        LocationPreferences.saveLocationForStartingPoint(this, locationFeature, true);

        editText.setText(text);
        editText.setSelection(text.length());
        toggleRecyclerView(false);
        hideKeyboard();
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
        dialogBinding.alertDialogFavListRv.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLocationSelected(String locationName, double latitude, double longitude) {
        // Handle the data received from the dialog
        Log.d("SelectedLocation", "Name: " + locationName + ", Lat: " + latitude + ", Long: " + longitude);
        // Update UI or use the data as needed
        binding.actvFavListHomeTv.setText(locationName);
    }

    private void setupRecyclerView() {
        adapter_fav_location = new Adapter_fav_location(new ArrayList<>());
        binding.actvFavListRv.setLayoutManager(new LinearLayoutManager(this));
        binding.actvFavListRv.setAdapter(adapter_fav_location);
    }
}
