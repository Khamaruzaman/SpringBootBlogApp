package com.example.BlogApp.model;

import com.example.BlogApp.utils.UUIDGenerator.Identifiable;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post implements Identifiable {
    @Id
    private UUID id;
    private String title;
    private String content;

    @Field("authorId")
    private UUID authorId;
    private List<String> tags;
    private long views;
    private boolean published;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
