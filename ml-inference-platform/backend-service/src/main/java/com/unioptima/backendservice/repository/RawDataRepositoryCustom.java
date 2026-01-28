package com.unioptima.backendservice.repository;

import org.bson.Document;

import java.util.List;


public interface RawDataRepositoryCustom {
    Document findLatestWithSelectedFeatures(String idOSYM, List<String> features);

    List<Document> findAllByLatestYear();
}
