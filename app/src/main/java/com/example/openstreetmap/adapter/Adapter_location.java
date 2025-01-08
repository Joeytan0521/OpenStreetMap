package com.example.openstreetmap.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openstreetmap.R;
import com.example.openstreetmap.databinding.RecyclerItemSearchResultBinding;
import com.example.openstreetmap.model.Location;
import com.example.openstreetmap.network.OnLocationSelectedListener;

import java.util.List;

public class Adapter_location extends RecyclerView.Adapter<Adapter_location.ViewHolder> {

    private final List<Location.LocationFeature> locationList;
    private final OnItemClickListener listener;
    private OnLocationSelectedListener listener2;


    public Adapter_location(List<Location.LocationFeature> locationList, OnItemClickListener listener) {
        this.locationList = locationList;
        this.listener = listener;
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener2) {
        this.listener2 = listener2;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerItemSearchResultBinding binding = RecyclerItemSearchResultBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location.LocationFeature location = locationList.get(position);

        String name = location.getProperties() != null ? location.getProperties().getName() : "Unknown Location";
        String region = location.getProperties() != null ? location.getProperties().getRegion() : "Unknown Region";

        holder.binding.recyclerItemSearchResultCityTv.setText(name);
        holder.binding.recyclerItemSearchResultRegionTv.setText(region);

        if (location.isPreviouslySelected()) {
            holder.binding.recyclerItemSearchResultIcIv.setImageResource(R.drawable.ic_previous_selection);
            holder.binding.recyclerItemSearchResultIcIv.setVisibility(View.VISIBLE);
        } else if ("Current Location".equals(name)) {
            holder.binding.recyclerItemSearchResultIcIv.setImageResource(R.drawable.ic_gps_grey);
            holder.binding.recyclerItemSearchResultIcIv.setVisibility(View.VISIBLE);
        } else {
            holder.binding.recyclerItemSearchResultIcIv.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(location));

    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerItemSearchResultBinding binding;

        public ViewHolder(RecyclerItemSearchResultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Location.LocationFeature feature);
    }

    public void updateData(List<Location.LocationFeature> newFeatures) {
        locationList.clear();
        locationList.addAll(newFeatures);
        notifyDataSetChanged();
    }
}
