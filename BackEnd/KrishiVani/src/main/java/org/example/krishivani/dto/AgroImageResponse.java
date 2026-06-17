package org.example.krishivani.dto;

import lombok.Data;

@Data
public class AgroImageResponse {
    private Long dt;
    private String type;
    private ImageData image;

    @Data
    public static class ImageData {
        private String truecolor;
        private String falsecolor;
        private String ndvi;
        private String evi;
    }
}
