package com.example.BlogApp.utils;

import com.example.BlogApp.model.User;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UserUUIDCallback implements BeforeConvertCallback<User> {

    @Override
    public User onBeforeConvert(User entity, String collection) {
        if (entity.getId() == null) {
            // Logic: If ID is missing, generate one now.
            // Using reflection/setters depends on your entity structure
             entity.setId(UUID.randomUUID());
        }
        return entity;
    }
}
