package com.example.BlogApp.repo;

import com.example.BlogApp.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepo extends MongoRepository<Post, UUID> {
    List<Post> findByTitleContaining(String title);
    List<Post> findByAuthorId(UUID authorId);
    List<Post> findByPublishedTrue(boolean published);
}
