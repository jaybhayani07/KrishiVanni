package org.example.krishivani.controller;

import lombok.RequiredArgsConstructor;
import org.example.krishivani.model.Bulletin;
import org.example.krishivani.repository.BulletinRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advisories")
@RequiredArgsConstructor
public class AdvisoryController {

    private final BulletinRepository bulletinRepository;

    @GetMapping("/bulletins")
    public ResponseEntity<List<Bulletin>> getBulletins() {
        return ResponseEntity.ok(bulletinRepository.findAll());
    }

    @PostMapping("/mock-data")
    public ResponseEntity<String> loadMockData() {
        if (bulletinRepository.count() == 0) {
            bulletinRepository.save(new Bulletin(null, "કપાસમાં ગુલાબી ઈયળ નિયંત્રણ", "Pink Bollworm Control in Cotton", "જંતુ નિયંત્રણ", "#E53935", "#FFEBEE", "૨૫ મે ૨૦૨૬", "2.4 MB"));
            bulletinRepository.save(new Bulletin(null, "મગફળીમાં ટીકા રોગ વ્યવસ્થાપન", "Tikka Disease Management in Groundnut", "રોગ વ્યવસ્થાપન", "#E65100", "#FFF3E0", "૨૩ મે ૨૦૨૬", "1.8 MB"));
            bulletinRepository.save(new Bulletin(null, "ખરીફ ૨૦૨૬ ની તૈયારી માર્ગદર્શિકા", "Kharif 2026 Preparation Guidelines", "સામાન્ય સલાહ", "#2E7D32", "#E8F5E9", "૨૦ મે ૨૦૨૬", "3.2 MB"));
            return ResponseEntity.ok("Mock advisory data loaded successfully.");
        }
        return ResponseEntity.ok("Mock advisory data already exists.");
    }
}
