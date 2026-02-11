package com.unioptima.backendservice.controller;

import com.unioptima.backendservice.dto.PredictRequest;
import com.unioptima.backendservice.service.DemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demand")
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    @GetMapping("/{idOSYM}/features")
    public ResponseEntity<List<Double>> getModelFeatures(@PathVariable String idOSYM) {
        return ResponseEntity.ok(demandService.getModelFeatures(idOSYM, null));
    }

    @PostMapping("/predict")
    public ResponseEntity<Double> getModelPrediction(@RequestBody PredictRequest req) {
        return ResponseEntity.ok(demandService.predictDemand(req.idOSYM().trim()));
    }
}
