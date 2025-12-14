package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.RawData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawDataRepository extends MongoRepository<RawData, String> {
}
