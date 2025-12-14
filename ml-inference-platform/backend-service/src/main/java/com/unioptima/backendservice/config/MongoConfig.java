package com.unioptima.backendservice.config;


import com.unioptima.backendservice.component.EncodedBaseRankingDataReadConverter;
import com.unioptima.backendservice.component.EncodedDemandDataReadConverter;
import com.unioptima.backendservice.component.RawDataReadConverter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions(
            EncodedDemandDataReadConverter demandConverter,
            RawDataReadConverter rawConverter,
            EncodedBaseRankingDataReadConverter rankingConverter) {

        return new MongoCustomConversions(Arrays.asList(
                demandConverter,
                rawConverter,
                rankingConverter
        ));
    }
}