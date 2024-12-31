package com.example.openstreetmap.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Directions {
    @SerializedName("features")
    private List<Feature> features;

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public static class Feature {
        @SerializedName("geometry")
        private Geometry geometry;

        @SerializedName("properties")
        private Properties properties;

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public static class Geometry {
            @SerializedName("type")
            private String type;

            @SerializedName("coordinates")
            private List<List<Double>> coordinates;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public List<List<Double>> getCoordinates() {
                return coordinates;
            }

            public void setCoordinates(List<List<Double>> coordinates) {
                this.coordinates = coordinates;
            }
        }

        public static class Properties {
            @SerializedName("segments")
            private List<Segment> segments;

            public List<Segment> getSegments() {
                return segments;
            }

            public void setSegments(List<Segment> segments) {
                this.segments = segments;
            }

            public static class Segment {
                @SerializedName("distance")
                private double distance;

                @SerializedName("duration")
                private double duration;

                public double getDistance() {
                    return distance;
                }

                public void setDistance(double distance) {
                    this.distance = distance;
                }

                public double getDuration() {
                    return duration;
                }

                public void setDuration(double duration) {
                    this.duration = duration;
                }
            }
        }
    }
}
