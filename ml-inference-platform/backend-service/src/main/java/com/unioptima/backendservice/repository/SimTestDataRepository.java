package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.SimTestData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SimTestDataRepository extends MongoRepository<SimTestData, String> {

    List<SimTestData> findAll();

}
