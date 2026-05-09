package com.example.BlogApp.repo;

import com.example.BlogApp.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface PostRepo extends MongoRepository<Post, UUID> {
    Page<Post> findByPublishedTrue(Pageable pageable);
    Page<Post> findByAuthorId(UUID authorId, Pageable pageable);
    Page<Post> findByAuthorIdAndPublishedTrue(UUID authorId, Pageable pageable);
    Page<Post> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);
}
