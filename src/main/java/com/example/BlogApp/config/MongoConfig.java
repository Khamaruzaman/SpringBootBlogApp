package com.example.BlogApp.config;

import org.bson.UuidRepresentation;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Configuration for MongoDB behavior: UUID representation and auditing.
 * Index creation is handled by a dedicated runtime component to avoid
 * constructor injection cycles during auto-configuration.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer customizer() {
        return builder -> builder.uuidRepresentation(UuidRepresentation.STANDARD);
    }
}
