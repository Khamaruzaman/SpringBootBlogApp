package com.example.BlogApp.repo;

import com.example.BlogApp.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepo extends MongoRepository<User, UUID> {
}
