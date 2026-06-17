package org.example.krishivani.controller;

import lombok.RequiredArgsConstructor;
import org.example.krishivani.model.HomeDashboardData;
import org.example.krishivani.service.HomeDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeDashboardService homeDashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<HomeDashboardData> getDashboardData(
            @RequestParam(required = false, defaultValue = "21.5222") double lat,
            @RequestParam(required = false, defaultValue = "70.4579") double lon) {
            
        HomeDashboardData dashboardData = homeDashboardService.getDashboardData(lat, lon);
        return ResponseEntity.ok(dashboardData);
    }

    @PostMapping("/mock-data")
    public ResponseEntity<String> loadMockData() {
        String result = homeDashboardService.loadMockData();
        return ResponseEntity.ok(result);
    }
}
