package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.AbstractIntegrationTest;
import com.unioptima.backendservice.model.BaseRankingMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class BaseRankingMetadataRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private BaseRankingMetadataRepository repository;

    @Test
    void testFindTopBy() {
        // Given
        BaseRankingMetadata metadata = new BaseRankingMetadata();
        metadata.setId("1");
        metadata.setFeatures(List.of("feature1", "feature2"));
        // Assuming there are other fields, filling basic ones for now based on what I
        // recall or can guess,
        // will check model definition if compilation fails, but looking at file listing
        // earlier, it seems fine.
        // Actually, let's just set what we know.

        repository.save(metadata);

        // When
        Optional<BaseRankingMetadata> result = repository.findTopBy();

        // Then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("1", result.get().getId());
        Assertions.assertEquals(2, result.get().getFeatures().size());
    }
}
