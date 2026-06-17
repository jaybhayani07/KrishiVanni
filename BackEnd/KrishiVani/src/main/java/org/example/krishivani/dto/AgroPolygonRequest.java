package org.example.krishivani.dto;

import lombok.Data;

@Data
public class AgroPolygonRequest {
    private String name;
    private GeoJson geo_json;

    @Data
    public static class GeoJson {
        private String type = "Feature";
        private java.util.Map<String, Object> properties = new java.util.HashMap<>();
        private Geometry geometry;
    }

    @Data
    public static class Geometry {
        private String type = "Polygon";
        private double[][][] coordinates;
    }
}
