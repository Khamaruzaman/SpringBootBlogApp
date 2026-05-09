package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.postDTO.CreatePostRequest;
import com.example.BlogApp.DTO.postDTO.PostDTO;
import com.example.BlogApp.DTO.postDTO.UpdatePostRequest;
import com.example.BlogApp.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Endpoints for managing blog posts")
public class PostController {
    private PostService postService;

    @GetMapping
    @Operation(summary = "Get all posts with pagination", description = "Retrieve a paginated list of all blog posts")
    public ResponseEntity<AuthResponse<Page<PostDTO>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.getAllPosts(pageable);

        AuthResponse<Page<PostDTO>> response = AuthResponse.<Page<PostDTO>>builder()
                .data(posts)
                .success(true)
                .message("Posts retrieved successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{postId}")
    @PreAuthorize("@securityService.canViewUnpublishedPost(#postId)")
    @Operation(summary = "Get post by ID", description = "Retrieve a single blog post by its unique identifier")
    public ResponseEntity<AuthResponse<PostDTO>> getPostById(@PathVariable UUID postId) {
        PostDTO post = postService.getPostById(postId);

        AuthResponse<PostDTO> response = AuthResponse.<PostDTO>builder()
                .data(post)
                .success(true)
                .message("Post retrieved successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    @Operation(summary = "Create a new post", description = "Create a new blog post with the provided details")
    public ResponseEntity<AuthResponse<PostDTO>> createPost(@Valid @RequestBody CreatePostRequest request) {
        AuthResponse<PostDTO> response = AuthResponse.<PostDTO>builder()
                .success(true)
                .data(postService.createPost(request))
                .message("Post created successfully")
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("@securityService.isPostAuthor(#postId)")
    @Operation(summary = "Update an existing post", description = "Update the details of an existing blog post. Only the author of the post can perform this operation.")
    public ResponseEntity<AuthResponse<PostDTO>> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        PostDTO updatedPost = postService.updatePost(postId, request);

        AuthResponse<PostDTO> response = AuthResponse.<PostDTO>builder()
                .data(updatedPost)
                .success(true)
                .message("Post updated successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("@securityService.canDeletePost(#postId)")
    @Operation(summary = "Delete a post", description = "Delete an existing blog post. Only the author of the post or an admin can perform this operation.")
    public ResponseEntity<AuthResponse<Void>> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);

        AuthResponse<Void> response = AuthResponse.<Void>builder()
                .success(true)
                .message("Post deleted successfully")
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get posts by author", description = "Retrieve a paginated list of blog posts created by a specific author")
    public ResponseEntity<AuthResponse<Page<PostDTO>>> getPostsByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.getPostsByAuthor(authorId, pageable);

        AuthResponse<Page<PostDTO>> response = AuthResponse.<Page<PostDTO>>builder()
                .data(posts)
                .success(true)
                .message("Posts by author retrieved successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts by keyword", description = "Search for blog posts that contain the specified keyword in the title or content")
    public ResponseEntity<AuthResponse<Page<PostDTO>>> searchPosts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.searchPosts(keyword, pageable);

        AuthResponse<Page<PostDTO>> response = AuthResponse.<Page<PostDTO>>builder()
                .data(posts)
                .success(true)
                .message("Posts searched successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{postId}/publish")
    @PreAuthorize("@securityService.isPostAuthor(#postId)")
    @Operation(summary = "Publish a post", description = "Publish a blog post, making it visible to the public. Only the author of the post can perform this operation.")
    public ResponseEntity<AuthResponse<PostDTO>> publishPost(@PathVariable UUID postId) {
        PostDTO publishedPost = postService.publishPost(postId);

        AuthResponse<PostDTO> response = AuthResponse.<PostDTO>builder()
                .data(publishedPost)
                .success(true)
                .message("Post published successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{postId}/unpublish")
    @PreAuthorize("@securityService.isPostAuthor(#postId)")
    @Operation(summary = "Unpublish a post", description = "Unpublish a blog post, making it invisible to the public. Only the author of the post can perform this operation.")
    public ResponseEntity<AuthResponse<PostDTO>> unpublishPost(@PathVariable UUID postId) {
        PostDTO unpublishedPost = postService.unpublishPost(postId);

        AuthResponse<PostDTO> response = AuthResponse.<PostDTO>builder()
                .data(unpublishedPost)
                .success(true)
                .message("Post unpublished successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
