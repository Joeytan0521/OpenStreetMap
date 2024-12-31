package com.example.openstreetmap.util;

import com.example.openstreetmap.model.Location;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocationDeserializer implements JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Location location = new Location();
        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray features = jsonObject.getAsJsonArray("features");

        List<Location.LocationFeature> locationFeatures = new ArrayList<>();
        for (JsonElement feature : features) {
            JsonObject featureObject = feature.getAsJsonObject();
            JsonObject properties = featureObject.getAsJsonObject("properties");

            String name = properties.has("name") ? properties.get("name").getAsString() : "Unknown Name";
            String region = properties.has("region") ? properties.get("region").getAsString() : "Unknown Region";

            Location.LocationFeature locationFeature = new Location.LocationFeature();
            Location.LocationFeature.Properties locationProperties = new Location.LocationFeature.Properties();
            locationProperties.setName(name);
            locationProperties.setRegion(region);

            locationFeature.setProperties(locationProperties);

            locationFeatures.add(locationFeature);
        }
        location.setFeatures(locationFeatures);
        return location;
    }
}
