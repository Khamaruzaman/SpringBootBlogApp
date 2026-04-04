package com.example.BlogApp.DTO.commentDTO;

import com.example.BlogApp.DTO.userDTO.UserDTO;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentDTO {
    private UUID id;
    private String content;
    private UserDTO author;
    private UUID postId;
    private Instant createdAt;
}
