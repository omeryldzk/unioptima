package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.repository.BaseRankingMetadataRepository;
import com.unioptima.backendservice.service.BaseRankingMetadataService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BaseRankingMetadataServiceImpl implements BaseRankingMetadataService {

    private final BaseRankingMetadataRepository repository;

    // IN-MEMORY CACHE
    private List<String> cachedFeatures = Collections.emptyList();

    public BaseRankingMetadataServiceImpl(BaseRankingMetadataRepository repository) {
        this.repository = repository;
    }

    /**
     * This runs automatically once when the application starts.
     * It loads the DB data into the private variables above.
     */
    @PostConstruct
    public void loadMetadata() {
        System.out.println(" Loading BaseRanking Metadata into Memory...");

        repository.findTopBy().ifPresentOrElse(meta -> {
            // 1. Cache the features
            this.cachedFeatures = meta.getFeatures();
            System.out.println(" BaseRanking Metadata Loaded. Features count: "
                    + (cachedFeatures != null ? cachedFeatures.size() : 0));
        }, () -> {
            System.err.println(" WARNING: No BaseRankingMetadata document found in MongoDB!");
        });
    }

    // --- Public Access Methods ---

    @Override
    public List<String> getFeatures() {
        return cachedFeatures;
    }

    /**
     * Optional: Call this if you manually update the DB and need to refresh the
     * cache
     * without restarting the server.
     */
    public void refreshCache() {
        loadMetadata();
    }
}
