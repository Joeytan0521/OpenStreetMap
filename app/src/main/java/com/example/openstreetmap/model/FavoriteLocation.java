package com.example.openstreetmap.model;

public class FavoriteLocation {
    private String name;
    private String region;

    public FavoriteLocation(String name, String region) {
        this.name = name;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }
}

