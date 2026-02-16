package com.example.BlogApp.config;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * Creates required MongoDB indexes at application startup using MongoTemplate.
 * Implemented as an ApplicationRunner to avoid constructor-time auto-config cycles.
 */
@Component
public class MongoIndexesCreator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexesCreator.class);
    private final MongoTemplate mongoTemplate;

    public MongoIndexesCreator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        try {
            // Ensure unique index on username and email in users collection
            IndexOperations usersIndexOps = mongoTemplate.indexOps("users");
            usersIndexOps.createIndex(new Index().on("username", Sort.Direction.ASC).unique());
            usersIndexOps.createIndex(new Index().on("email", Sort.Direction.ASC).unique());
            log.info("Ensured indexes for 'users' collection (username, email)");

            // Ensure index for posts collection (e.g., title, author)
            IndexOperations postsIndexOps = mongoTemplate.indexOps("posts");
            postsIndexOps.createIndex(new Index().on("authorId", Sort.Direction.ASC));
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
}
