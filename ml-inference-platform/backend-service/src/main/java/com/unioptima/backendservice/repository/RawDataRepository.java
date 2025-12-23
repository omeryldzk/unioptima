package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.EncodedDemandData;
import com.unioptima.backendservice.model.RawData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RawDataRepository extends MongoRepository<RawData, String> {
    Optional<RawData> findTopByIdStartingWithOrderByIdDesc(String prefix);
}
