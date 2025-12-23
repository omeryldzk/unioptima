package com.unioptima.backendservice.controller;

import com.unioptima.backendservice.service.DemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demand")
public class DemandController {

    @Autowired
    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @GetMapping("/{idOSYM}/features")
    public ResponseEntity<List<Double>> getModelFeatures(@PathVariable String idOSYM) {
        return ResponseEntity.ok(demandService.getModelFeatures(idOSYM));
    }

    @PostMapping("predict")
    public ResponseEntity<Double> getModelPrediction(@RequestBody String idOSYM) {
        return ResponseEntity.ok(demandService.predictDemand(idOSYM));
    }
}
