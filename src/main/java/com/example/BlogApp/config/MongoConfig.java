package com.example.BlogApp.config;

import org.bson.UuidRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Ensures important indexes are created on application startup.
 * Uses collection names directly so this is independent of entity classes.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    public MongoConfig(MongoTemplate mongoTemplate) {
        try {
            // Ensure unique index on username and email in users collection
            IndexOperations usersIndexOps = mongoTemplate.indexOps("users");
            usersIndexOps.createIndex(new Index().on("username", Sort.Direction.ASC).unique());
            usersIndexOps.createIndex(new Index().on("email", Sort.Direction.ASC).unique());
            log.info("Ensured indexes for 'users' collection (username, email)");

            // Ensure index for posts collection (e.g., title, author)
            IndexOperations postsIndexOps = mongoTemplate.indexOps("posts");
            postsIndexOps.createIndex(new Index().on("authorId", Sort.Direction.ASC));
            // Create a simple ascending index on title (use TextIndexDefinition for full text search if needed)
            postsIndexOps.createIndex(new Index().on("title", Sort.Direction.ASC));
            log.info("Ensured basic indexes for 'posts' collection (authorId, title)");

            // Ensure index for comments collection (postId)
            IndexOperations commentsIndexOps = mongoTemplate.indexOps("comments");
            commentsIndexOps.createIndex(new Index().on("postId", Sort.Direction.ASC));
            log.info("Ensured index for 'comments' collection (postId)");

        } catch (Exception e) {
            log.warn("Failed to ensure MongoDB indexes at startup: {}", e.getMessage());
        }
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer customizer() {
        return builder -> builder.uuidRepresentation(UuidRepresentation.STANDARD);
    }
}
