package org.example.krishivani.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgroImageResponse {
    private Long dt;
    private String type;
    private ImageData image;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageData {
        private String truecolor;
        private String falsecolor;
        private String ndvi;
        private String evi;
    }
}
