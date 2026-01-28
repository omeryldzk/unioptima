package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.EncodedBaseRankingData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EncodedBaseRankingDataRepository extends MongoRepository<EncodedBaseRankingData, String>, EncodedBaseRankingDataRepositoryCustom {
    // Find documents starting with a prefix (e.g., idOSYM for all years)
    Optional<EncodedBaseRankingData> findTopByIdStartingWithOrderByIdDesc(String prefix);

}
