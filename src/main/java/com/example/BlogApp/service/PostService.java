package com.example.BlogApp.service;

import com.example.BlogApp.DTO.postDTO.CreatePostRequest;
import com.example.BlogApp.DTO.postDTO.PostDTO;
import com.example.BlogApp.DTO.postDTO.UpdatePostRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.Post;
import com.example.BlogApp.repo.PostRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PostService {
    private PostRepo postRepo;
    private UserService userService;

    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return postRepo.findByPublishedTrue(pageable)
                .map(this::mapPostToDTO);
    }

    public PostDTO getPostById(UUID postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Increment view count
        post.setViews(post.getViews() + 1);
        postRepo.save(post);

        return mapPostToDTO(post);
    }

    public PostDTO createPost(CreatePostRequest request) {
        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAuthorId(currentUser.getId());
        post.setTags(request.getTags());
        post.setPublished(false); // Default to unpublished
        post.setViews(0);

        Post savedPost = postRepo.save(post);
        return mapPostToDTO(savedPost);
    }

    public PostDTO updatePost(UUID postId, UpdatePostRequest request) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check if current user is the author
        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);
        if (!post.getAuthorId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own posts");
        }

        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getTags() != null) {
            post.setTags(request.getTags());
        }
        post.setUpdatedAt(Instant.now());

        Post updatedPost = postRepo.save(post);
        return mapPostToDTO(updatedPost);
    }

    public void deletePost(UUID postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check if current user is the author
        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);
        if (!post.getAuthorId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only delete your own posts");
        }

        postRepo.deleteById(postId);
    }

    public Page<PostDTO> getPostsByAuthor(UUID authorId, Pageable pageable) {
        return postRepo.findByAuthorId(authorId, pageable)
                .map(this::mapPostToDTO);
    }

    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        return postRepo.findByTitleContaining(keyword, pageable)
                .map(this::mapPostToDTO);
    }

    public PostDTO publishPost(UUID postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check if current user is the author
        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);
        if (!post.getAuthorId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only publish your own posts");
        }

        post.setPublished(true);
        post.setUpdatedAt(Instant.now());
        Post updatedPost = postRepo.save(post);
        return mapPostToDTO(updatedPost);
    }

    public PostDTO unpublishPost(UUID postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check if current user is the author
        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);
        if (!post.getAuthorId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only unpublish your own posts");
        }

        post.setPublished(false);
        post.setUpdatedAt(Instant.now());
        Post updatedPost = postRepo.save(post);
        return mapPostToDTO(updatedPost);
    }

    private PostDTO mapPostToDTO(Post post) {
        UserDTO authorDTO = userService.getUserById(post.getAuthorId());

        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setAuthor(authorDTO);
        postDTO.setTags(post.getTags());
        postDTO.setViews(post.getViews());
        postDTO.setCreatedAt(post.getCreatedAt());
        postDTO.setUpdatedAt(post.getUpdatedAt());
        return postDTO;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        return authentication.getName();
    }
}
