package com.unioptima.backendservice.service;

import com.unioptima.backendservice.model.DemandMetadata;
import com.unioptima.backendservice.repository.DemandMetadataRepository;
import com.unioptima.backendservice.service.impl.DemandMetadataServiceImpl;
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
class DemandMetadataServiceTest {

    @Mock
    private DemandMetadataRepository repository;

    @InjectMocks
    private DemandMetadataServiceImpl service;

    @Test
    void testLoadMetadata() {
        // Given
        DemandMetadata metadata = new DemandMetadata();
        metadata.setFeatures(List.of("d1", "d2"));
        Mockito.when(repository.findTopBy()).thenReturn(Optional.of(metadata));

        // When
        service.loadMetadata();

        // Then
        List<String> features = service.getFeatures();
        Assertions.assertEquals(2, features.size());
        Assertions.assertTrue(features.contains("d1"));
        Assertions.assertTrue(features.contains("d2"));
    }
}
