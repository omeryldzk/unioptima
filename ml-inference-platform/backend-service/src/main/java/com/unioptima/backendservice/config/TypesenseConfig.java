package com.unioptima.backendservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.typesense.api.Client;
import org.typesense.api.Configuration;
import org.typesense.resources.Node;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@org.springframework.context.annotation.Configuration
public class TypesenseConfig {

    @Value("${typesense.protocol}")
    private String protocol;

    @Value("${typesense.host}")
    private String host;

    @Value("${typesense.port}")
    private String port;

    @Value("${typesense.api-key}")
    private String apiKey;

    @Bean
    public Client typesenseClient() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(protocol, host, port));

        // Configure connection timeout and API key
        Configuration configuration = new Configuration(nodes, Duration.ofSeconds(2), apiKey);

        return new Client(configuration);
    }
}