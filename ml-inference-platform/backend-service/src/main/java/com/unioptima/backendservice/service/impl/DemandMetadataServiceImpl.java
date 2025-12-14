package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.repository.DemandMetadataRepository;
import com.unioptima.backendservice.service.DemandMetadataService;
import jakarta.annotation.PostConstruct; // or javax.annotation.PostConstruct for older Spring
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DemandMetadataServiceImpl implements DemandMetadataService {

    private final DemandMetadataRepository repository;

    // IN-MEMORY CACHE
    private List<String> cachedFeatures = Collections.emptyList();
    private Set<String> cachedMainIds = Collections.emptySet();

    public DemandMetadataServiceImpl(DemandMetadataRepository repository) {
        this.repository = repository;
    }

    /**
     * This runs automatically once when the application starts.
     * It loads the DB data into the private variables above.
     */
    @PostConstruct
    public void loadMetadata() {
        System.out.println(" Loading Demand Metadata into Memory...");

        repository.findTopBy().ifPresentOrElse(meta -> {
            // 1. Cache the features
            this.cachedFeatures = meta.getFeatures();

            // 2. Optimization: Convert List to HashSet for O(1) lookup speed
            if (meta.getMainIdOSYM() != null) {
                this.cachedMainIds = new HashSet<>(meta.getMainIdOSYM());
            }

            System.out.println(" Metadata Loaded. Main IDs count: " + cachedMainIds.size());
        }, () -> {
            System.err.println(" WARNING: No DemandMetadata document found in MongoDB!");
        });
    }

    // --- Public Access Methods ---

    public List<String> getFeatures() {
        return cachedFeatures;
    }

    /**
     * High-performance check if an ID exists in the main list.
     */
    public boolean isMainId(String idOSYM) {
        return cachedMainIds.contains(idOSYM);
    }

    /**
     * Optional: Call this if you manually update the DB and need to refresh the cache
     * without restarting the server.
     */
    public void refreshCache() {
        loadMetadata();
    }
}