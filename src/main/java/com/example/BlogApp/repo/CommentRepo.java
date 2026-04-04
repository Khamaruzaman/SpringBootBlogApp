package com.example.BlogApp.repo;

import com.example.BlogApp.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepo extends MongoRepository<Comment, UUID> {
    List<Comment> findByPostId(UUID postId);
    List<Comment> findByAuthorId(UUID authorId);
}
