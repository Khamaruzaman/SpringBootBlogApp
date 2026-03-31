package com.example.BlogApp.DTO;

import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDTO {
    private UUID id;
    private String title;
    private String content;
    private UserDTO author;
    private List<String> tags;
    private long views;
    private Instant createdAt;
    private Instant updatedAt;

}
