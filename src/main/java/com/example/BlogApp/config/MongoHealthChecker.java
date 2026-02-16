package com.example.BlogApp.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Pings MongoDB on startup to verify connectivity. Logs the result.
 */
@Component
public class MongoHealthChecker implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoHealthChecker.class);
    private final MongoTemplate mongoTemplate;

    public MongoHealthChecker(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Document result = mongoTemplate.executeCommand(new Document("ping", 1));
            log.info("MongoDB ping response: {}", result.toJson());
        } catch (Exception e) {
            log.error("Failed to ping MongoDB at startup: {}", e.getMessage());
            // Do not stop startup; just log. Consider failing the app in production if required.
        }
    }
}

