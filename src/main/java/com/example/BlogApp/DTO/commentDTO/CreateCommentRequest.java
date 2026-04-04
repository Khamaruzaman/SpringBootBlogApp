package com.example.BlogApp.DTO.commentDTO;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCommentRequest {
    private String content;
    private UUID authorId;
    private UUID postId;
}
