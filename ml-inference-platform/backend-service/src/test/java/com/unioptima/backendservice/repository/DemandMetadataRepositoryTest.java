package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.AbstractIntegrationTest;
import com.unioptima.backendservice.model.DemandMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class DemandMetadataRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private DemandMetadataRepository repository;

    @Test
    void testSaveAndFind() {
        // Given
        DemandMetadata metadata = new DemandMetadata();
        metadata.setId("demand1");
        // Add more fields if necessary based on the model

        repository.save(metadata);

        // When
        Optional<DemandMetadata> result = repository.findById("demand1");

        // Then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("demand1", result.get().getId());
    }
}
