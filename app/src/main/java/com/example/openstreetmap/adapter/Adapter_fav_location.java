package com.example.openstreetmap.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.openstreetmap.databinding.RecyclerItemFavListBinding;
import com.example.openstreetmap.model.FavoriteLocation;
import com.example.openstreetmap.model.Location;

import java.util.List;

public class Adapter_fav_location extends RecyclerView.Adapter<Adapter_fav_location.ViewHolder> {
    private final List<FavoriteLocation> favoriteLocations;

    public Adapter_fav_location(List<FavoriteLocation> favoriteLocations) {
        this.favoriteLocations = favoriteLocations;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerItemFavListBinding binding = RecyclerItemFavListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FavoriteLocation location = favoriteLocations.get(position);
        holder.binding.recyclerItemFavListTv.setText(location.getName());
        holder.binding.recyclerItemFavListTv2.setText(location.getRegion());
    }

    @Override
    public int getItemCount() {
        return favoriteLocations.size();
    }

    public void addLocation(FavoriteLocation location) {
        favoriteLocations.add(location);
        notifyItemInserted(favoriteLocations.size() - 1);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerItemFavListBinding binding;

        public ViewHolder(RecyclerItemFavListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

