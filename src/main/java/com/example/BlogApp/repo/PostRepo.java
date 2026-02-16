package com.example.BlogApp.repo;

import com.example.BlogApp.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PostRepo extends MongoRepository<Post, UUID> {
    Post findByTitleContaining(String title);
    Post findByAuthor(String author);
    Post findByPublishedTrue(boolean published);
}
