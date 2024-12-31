package com.example.openstreetmap.model;

import java.util.List;

public class Location {
    private List<LocationFeature> features;

    public List<LocationFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<LocationFeature> features) {
        this.features = features;
    }

    public static class LocationFeature {
        private Properties properties;
        private Geometry geometry;
        private boolean isPreviouslySelected;

        public boolean isPreviouslySelected() {
            return isPreviouslySelected;
        }

        public void setPreviouslySelected(boolean previouslySelected) {
            isPreviouslySelected = previouslySelected;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public static class Properties {
            private String name;
            private String region;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }
        }

        public static class Geometry {
            private double[] coordinates;

            public double[] getCoordinates() {
                return coordinates;
            }

            public void setCoordinates(double[] coordinates) {
                this.coordinates = coordinates;
            }

            public double getLongitude() {
                return coordinates != null && coordinates.length > 0 ? coordinates[0] : 0.0;
            }

            public double getLatitude() {
                return coordinates != null && coordinates.length > 1 ? coordinates[1] : 0.0;
            }
        }
    }
}
