package org.example.krishivani.controller;

import lombok.RequiredArgsConstructor;
import org.example.krishivani.model.DiseaseDetection;
import org.example.krishivani.repository.DiseaseDetectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/camera")
@RequiredArgsConstructor
public class CameraController {

    private final DiseaseDetectionRepository diseaseDetectionRepository;

    @PostMapping("/detect")
    public ResponseEntity<DiseaseDetection> detectDisease(@RequestParam("image") MultipartFile image) {



        List<DiseaseDetection> mocks = diseaseDetectionRepository.findAll();
        if (mocks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mocks.get(0));
    }

    @PostMapping("/mock-data")
    public ResponseEntity<String> loadMockData() {
        if (diseaseDetectionRepository.count() == 0) {
            diseaseDetectionRepository.save(new DiseaseDetection(
                    null,
                    "ટીકા રોગ", "Tikka Disease", "92%",
                    "ક્લોરોથાલોનીલ (૨ ગ્રામ/લિટર) નો છંટકાવ કરો.",
                    "Spray Chlorothalonil (2g/L)."
            ));
            return ResponseEntity.ok("Mock Camera AI data loaded successfully.");
        }
        return ResponseEntity.ok("Mock Camera AI data already exists.");
    }
}
