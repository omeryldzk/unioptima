package com.unioptima.backendservice.service;

import com.unioptima.backendservice.model.BaseRankingMetadata;
import com.unioptima.backendservice.repository.BaseRankingMetadataRepository;
import com.unioptima.backendservice.service.impl.BaseRankingMetadataServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BaseRankingMetadataServiceTest {

    @Mock
    private BaseRankingMetadataRepository repository;

    @InjectMocks
    private BaseRankingMetadataServiceImpl service;

    @Test
    void testLoadMetadata() {
        // Given
        BaseRankingMetadata metadata = new BaseRankingMetadata();
        metadata.setFeatures(List.of("f1", "f2"));
        Mockito.when(repository.findTopBy()).thenReturn(Optional.of(metadata));

        // When
        service.loadMetadata();

        // Then
        List<String> features = service.getFeatures();
        Assertions.assertEquals(2, features.size());
        Assertions.assertTrue(features.contains("f1"));
        Assertions.assertTrue(features.contains("f2"));
    }

    @Test
    void testLoadMetadataEmpty() {
        // Given
        Mockito.when(repository.findTopBy()).thenReturn(Optional.empty());

        // When
        service.loadMetadata();

        // Then
        List<String> features = service.getFeatures();
        Assertions.assertTrue(features.isEmpty());
    }
}
