package com.example.BlogApp.DTO.commentDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request object for creating a new comment")
public class CreateCommentRequest {
    @NotBlank(message = "Content must not be blank")
    private String content;
    private UUID postId;
}
