package org.example.krishivani.controller;

import lombok.RequiredArgsConstructor;
import org.example.krishivani.dto.IrrigationAdviceResponse;
import org.example.krishivani.dto.IrrigationRequest;
import org.example.krishivani.service.AdvancedIrrigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/irrigation")
@RequiredArgsConstructor
public class IrrigationController {

    private final AdvancedIrrigationService advancedIrrigationService;

    @PostMapping("/advice")
    public ResponseEntity<IrrigationAdviceResponse> getAdvice(@RequestBody IrrigationRequest request) {
        IrrigationAdviceResponse response = advancedIrrigationService.getIrrigationAdvice(request);
        return ResponseEntity.ok(response);
    }
}
