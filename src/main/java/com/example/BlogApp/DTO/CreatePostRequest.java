package com.example.BlogApp.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePostRequest {
    private String title;
    private String content;
    private List<String> tags;
}
