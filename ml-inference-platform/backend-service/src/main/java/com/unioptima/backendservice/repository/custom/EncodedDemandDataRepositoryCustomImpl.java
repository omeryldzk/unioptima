package com.unioptima.backendservice.repository.custom;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public class EncodedDemandDataRepositoryCustomImpl implements com.unioptima.backendservice.repository.EncodedDemandDataRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public EncodedDemandDataRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Document findLatestWithSelectedFeatures(String idOSYM, List<String> features) {
        Query query = new Query();

        // 1. FILTER: Logic for "Composite Key starting with idOSYM_"
        // We look for IDs like "101010_2023", "101010_2024"
        query.addCriteria(Criteria.where("_id").regex("^" + idOSYM + "_"));

        // 2. SORT & LIMIT: Logic for "Latest Year"
        query.with(Sort.by(Sort.Direction.DESC, "_id"));
        query.limit(1);

        // 3. PROJECTION: Logic for "Subset Features"
        // Always include _id so the object is valid
        query.fields().include("_id");
        query.fields().include("idOSYM");
        query.fields().include("academicYear");

        // Add dynamic features requested by metadata
        if (features != null) {
            for (String feature : features) {
                query.fields().include(feature);
            }
        }

        // 4. EXECUTE
        // Map directly to your Entity class. Fields not included will be null.
        return mongoTemplate.findOne(query, Document.class, "encoded_demand_data");
    }
}
