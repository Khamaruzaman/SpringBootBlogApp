package com.example.BlogApp.utils.UUIDGenerator;

import java.util.UUID;

public interface Identifiable {
    UUID getId();
    void setId(UUID id);
}
