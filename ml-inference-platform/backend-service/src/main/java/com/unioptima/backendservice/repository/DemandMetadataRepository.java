package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.DemandMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemandMetadataRepository extends MongoRepository<DemandMetadata, String> {
    // Fetches the first document it finds (since there is only one)
    Optional<DemandMetadata> findTopBy();

}
