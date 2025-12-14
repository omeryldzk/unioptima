package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.BaseRankingMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseRankingMetadataRepository extends MongoRepository<BaseRankingMetadata, String> {
    java.util.Optional<BaseRankingMetadata> findTopBy();
}
