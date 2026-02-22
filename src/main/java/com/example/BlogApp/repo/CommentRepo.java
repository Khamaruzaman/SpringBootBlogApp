package com.example.BlogApp.repo;

import com.example.BlogApp.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface CommentRepo extends MongoRepository<Comment, UUID> {
    Comment findByPostId(UUID postId);
    Comment findByAuthorId(UUID authorId);
}
