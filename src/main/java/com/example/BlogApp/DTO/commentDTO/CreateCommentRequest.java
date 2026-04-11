package com.example.BlogApp.DTO.commentDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for creating a new comment")
public class CreateCommentRequest {
    private String content;
    private UUID postId;
}
