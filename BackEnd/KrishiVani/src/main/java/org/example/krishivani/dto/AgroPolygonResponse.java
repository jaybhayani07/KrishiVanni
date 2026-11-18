package org.example.krishivani.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgroPolygonResponse {
    private String id;
    private String name;
    private double[] center;
}
