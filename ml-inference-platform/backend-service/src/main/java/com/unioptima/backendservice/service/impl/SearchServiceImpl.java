package com.unioptima.backendservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unioptima.backendservice.component.DataMapper;
import com.unioptima.backendservice.dto.FacetDto;
import com.unioptima.backendservice.dto.SearchDataDto;
import com.unioptima.backendservice.dto.SearchRequest;
import com.unioptima.backendservice.dto.SearchResponseDto;
import com.unioptima.backendservice.repository.RawDataRepository;
import com.unioptima.backendservice.service.SearchService;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.typesense.api.Client;
import org.typesense.api.FieldTypes;
import org.typesense.api.exceptions.ObjectAlreadyExists;
import org.typesense.api.exceptions.ObjectNotFound;
import org.typesense.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final Client client;
    private final RawDataRepository databaseRepository;

    private final DataMapper mapper;

    private final String COLLECTION_NAME = "programs";


    public SearchServiceImpl(Client client, RawDataRepository databaseRepository, DataMapper mapper) {
        this.client = client;
        this.databaseRepository = databaseRepository;
        this.mapper = mapper;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndex() throws Exception {
        log.info("Initializing Typesense Collection and Indexing Data...");
        createCollection(); // Ensure collection exists

        // Fetch all data from your database
        List<Document> allData = databaseRepository.findAllByLatestYear();
        log.info("Fetched {} records from the database for indexing.", allData.size());

        // 2. Map all data to DTOs in memory
        List<SearchDataDto> batchList = allData.stream()
                .map((doc) -> mapper.toSearchDataDto(doc))
                .collect(Collectors.toList());

        // 3. Send to Typesense in one shot (Batch Import)
        bulkIndex(batchList);

    }

    // Optimized Batch Method
    public void bulkIndex(List<SearchDataDto> documents) throws Exception {
        log.info("Starting batch indexing of {} documents into Typesense...", documents.size());
        ImportDocumentsParameters importParams = new ImportDocumentsParameters();
        importParams.action("upsert"); // Overwrite existing docs with same ID

        // Typesense returns a JSON string result for the batch operation
        String result = client.collections(COLLECTION_NAME)
                .documents()
                .import_(documents, importParams);

        log.info("Batch indexing completed. Result: {}", result);
    }


    // 1. Initialize Collection (Schema)
    public void createCollection() throws Exception {
        // Check if collection exists to avoid overwriting
        try {
            log.info("Checking if Typesense collection '{}' exists...", COLLECTION_NAME);
            client.collections(COLLECTION_NAME).retrieve();
        } catch (ObjectNotFound notFound) {
            log.info("Collection '{}' not found. Creating...", COLLECTION_NAME);
            // create below
        }

        log.info("Collection '{}' does not exist. Creating new collection...", COLLECTION_NAME);
            CollectionSchema collectionSchema = new CollectionSchema();
            collectionSchema.name(COLLECTION_NAME);

            // Define fields and their types
            collectionSchema.addFieldsItem(new Field().name("universityName").type(FieldTypes.STRING).facet(true));
            collectionSchema.addFieldsItem(new Field().name("departmentName").type(FieldTypes.STRING).facet(true));
            collectionSchema.addFieldsItem(new Field().name("language").type(FieldTypes.STRING).facet(true));
            collectionSchema.addFieldsItem(new Field().name("faculty").type(FieldTypes.STRING).facet(true));
            collectionSchema.addFieldsItem(new Field().name("scholarshipRate").type(FieldTypes.STRING).facet(true));
            collectionSchema.addFieldsItem(new Field().name("idOSYM").type(FieldTypes.STRING).facet(false));

        try {
            client.collections().create(collectionSchema);
            log.info("Collection '{}' created.", COLLECTION_NAME);
        } catch (ObjectAlreadyExists ignore) {
            // Handles race condition if multiple app instances start at same time
            log.info("Collection '{}' already exists (race). Continuing.", COLLECTION_NAME);
        }
    }

    @Override
    public SearchResponseDto search(SearchRequest req) throws Exception {
        log.info("Executing search with request: {}", req);
        SearchParameters searchParameters = new SearchParameters();

        // 1. Determine query_by based on current UX focus
        String queryBy = req.getTargetField();

        // 1.1 Handle Query (q)
        // Match everything if no text provided
        if (req.getQ() == null || req.getQ().isBlank()) {
            log.info("No search text provided, matching all documents.");
            searchParameters.q("*"); // Match everything if no text provided
        } else {
            log.info("Searching for text: {}", req.getQ());
            searchParameters.q(req.getQ());
        }

        // 2. Build filter_by from accumulated locks
        List<String> filters = new ArrayList<>();
        req.getLocks().forEach((field, value) -> {
            // Construct strict filter syntax
            filters.add(field + ":=" + value);
        });
        log.info("Constructed filters: {}", filters);

        searchParameters
                .queryBy(queryBy)
                .filterBy(String.join(" && ", filters))
                // Technical optimization:
                // facet_by as Server-Side Aggregation (GROUP BY)
                // By adding .facetBy for the target field,
                // Typesense will return unique values for that field
                .facetBy(req.getTargetField());

        // 1. Execute Search
        SearchResult result = client.collections(COLLECTION_NAME).documents().search(searchParameters);

        // 2. Map Hits to SearchDataDto
        List<SearchDataDto> itemDTOs = result.getHits().stream()
                .map(hit -> mapper.getMapper().convertValue(hit.getDocument(), SearchDataDto.class))
                .collect(Collectors.toList());

        // 3. Map Facets to FacetDto
        // FacetDto contains value and count
        // Which we will return to the frontend
        // To explain the user how many options are available
        List<FacetDto> facetDTOs = new ArrayList<>();

        // Check if facets exist for the requested target field
        // keep how many options available for the target field
        if (result.getFacetCounts() != null) {
            result.getFacetCounts().stream()
                    .filter(f -> f.getFieldName().equals(req.getTargetField()))
                    .findFirst()
                    .ifPresent(facetCounts -> {
                        facetCounts.getCounts().forEach(countObj -> {
                            facetDTOs.add(new FacetDto(countObj.getValue(), countObj.getCount()));
                        });
                    });
        }

        // 4. Build Final Response
        SearchResponseDto response = new SearchResponseDto();
        response.setTotalHits(result.getFound());
        response.setPage(result.getPage());
        response.setItems(itemDTOs);
        response.setFacets(facetDTOs);

        return response;
    }

}