package com.example.BlogApp.DTO.postDTO;

import com.example.BlogApp.DTO.userDTO.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Data Transfer Object for blog post details")
public class PostDTO {
    private UUID id;
    private String title;
    private String content;
    private UserDTO author;
    private List<String> tags;
    private long views;
    private boolean published;
    private Instant createdAt;
    private Instant updatedAt;

}
