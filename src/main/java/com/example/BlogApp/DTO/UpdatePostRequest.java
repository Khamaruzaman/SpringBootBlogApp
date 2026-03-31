package com.example.BlogApp.DTO;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePostRequest {
    private UUID id;
    private String title;
    private String content;
    private List<String> tags;
}
