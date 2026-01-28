package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.EncodedDemandData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EncodedDemandDataRepository extends MongoRepository<EncodedDemandData, String>, EncodedDemandDataRepositoryCustom {

    /**
     * Finds the latest data entry for a specific Program ID.
     * * Logic:
     * 1. Matches IDs starting with "101010_"
     * 2. Sorts them Descending ("101010_2024", "101010_2023")
     * 3. Returns the Top (First) one -> 2024.
     *
     * @param prefix The idOSYM + "_" (e.g., "101010_")
     * @return The document with the highest year.
     */
    Optional<EncodedDemandData> findTopByIdStartingWithOrderByIdDesc(String prefix);
}
