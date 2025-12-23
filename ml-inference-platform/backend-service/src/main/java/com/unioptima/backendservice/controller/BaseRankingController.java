package com.unioptima.backendservice.controller;

import com.unioptima.backendservice.service.BaseRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base-ranking")
public class BaseRankingController {

    @Autowired
    private final BaseRankingService baseRankingService;

    public BaseRankingController(BaseRankingService baseRankingService) {
        this.baseRankingService = baseRankingService;
    }

    @GetMapping("/{idOSYM}/features")
    public ResponseEntity<List<Double>> getModelFeatures(@PathVariable String idOSYM) {
        return ResponseEntity.ok(baseRankingService.getModelFeatures(idOSYM));
    }

    @PostMapping("predict")
    public ResponseEntity<Double> getModelPrediction(@RequestBody String idOSYM) {
        return ResponseEntity.ok(baseRankingService.predictRanking(idOSYM));
    }
}
