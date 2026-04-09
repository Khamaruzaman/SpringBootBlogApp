package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.postDTO.CreatePostRequest;
import com.example.BlogApp.DTO.postDTO.PostDTO;
import com.example.BlogApp.DTO.postDTO.UpdatePostRequest;
import com.example.BlogApp.service.PostService;
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
public class PostController {
    private PostService postService;

    @GetMapping
    public ResponseEntity<AuthResponse<Page<PostDTO>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.getAllPosts(pageable);

        AuthResponse<Page<PostDTO>> response = new AuthResponse<>();
        response.setData(posts);
        response.setSuccess(true);
        response.setMessage("Posts retrieved successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<AuthResponse<PostDTO>> getPostById(@PathVariable UUID postId) {
        PostDTO post = postService.getPostById(postId);

        AuthResponse<PostDTO> response = new AuthResponse<>();
        response.setData(post);
        response.setSuccess(true);
        response.setMessage("Post retrieved successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<AuthResponse<PostDTO>> createPost(@Valid @RequestBody CreatePostRequest request) {
        AuthResponse<PostDTO> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setData(postService.createPost(request));
        response.setMessage("Post created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("@securityService.isPostAuthor(#postId)")
    public ResponseEntity<AuthResponse<PostDTO>> updatePost(@PathVariable UUID postId, @Valid @RequestBody UpdatePostRequest request) {
        PostDTO updatedPost = postService.updatePost(postId, request);

        AuthResponse<PostDTO> response = new AuthResponse<>();
        response.setData(updatedPost);
        response.setSuccess(true);
        response.setMessage("Post updated successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("@securityService.canDeletePost(#postId)")
    public ResponseEntity<AuthResponse<Void>> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);

        AuthResponse<Void> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("Post deleted successfully");
        response.setData(null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<AuthResponse<Page<PostDTO>>> getPostsByAuthor(
            @PathVariable UUID authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.getPostsByAuthor(authorId, pageable);

        AuthResponse<Page<PostDTO>> response = new AuthResponse<>();
        response.setData(posts);
        response.setSuccess(true);
        response.setMessage("Posts by author retrieved successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<AuthResponse<Page<PostDTO>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PostDTO> posts = postService.searchPosts(keyword, pageable);

        AuthResponse<Page<PostDTO>> response = new AuthResponse<>();
        response.setData(posts);
        response.setSuccess(true);
        response.setMessage("Posts searched successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{postId}/publish")
    @PreAuthorize("@securityService.isPostAuthor(#postId)")
    public ResponseEntity<AuthResponse<PostDTO>> publishPost(@PathVariable UUID postId) {
        PostDTO publishedPost = postService.publishPost(postId);

        AuthResponse<PostDTO> response = new AuthResponse<>();
        response.setData(publishedPost);
        response.setSuccess(true);
        response.setMessage("Post published successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{postId}/unpublish")
    @PreAuthorize("@securityService.isPostAuthor(#postId)")
    public ResponseEntity<AuthResponse<PostDTO>> unpublishPost(@PathVariable UUID postId) {
        PostDTO unpublishedPost = postService.unpublishPost(postId);

        AuthResponse<PostDTO> response = new AuthResponse<>();
        response.setData(unpublishedPost);
        response.setSuccess(true);
        response.setMessage("Post unpublished successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
