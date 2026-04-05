package com.example.BlogApp.utils.UUIDGenerator;

import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenericUUIDCallback implements BeforeConvertCallback<Identifiable> {

    @Override
    public Identifiable onBeforeConvert(Identifiable entity, @NonNull String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return entity;
    }
}
