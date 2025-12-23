package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.BaseRankingMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseRankingMetadataRepository extends MongoRepository<BaseRankingMetadata, String> {
    Optional<BaseRankingMetadata> findTopBy();
}
