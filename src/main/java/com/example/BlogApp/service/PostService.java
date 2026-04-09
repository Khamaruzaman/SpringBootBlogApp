package com.example.BlogApp.service;

import com.example.BlogApp.DTO.postDTO.CreatePostRequest;
import com.example.BlogApp.DTO.postDTO.PostDTO;
import com.example.BlogApp.DTO.postDTO.UpdatePostRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.Post;
import com.example.BlogApp.repo.PostRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PostService {
    private PostRepo postRepo;
    private UserService userService;

    public Page<PostDTO> getAllPosts(Pageable pageable) {
        try {
            Page<PostDTO> posts = postRepo.findByPublishedTrue(pageable)
                    .map(this::mapPostToDTO);
            log.info("Retrieved {} posts (page {}, size {})", posts.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize());
            return posts;
        } catch (Exception e) {
            log.error("Error retrieving all posts: {}", e.getMessage());
            throw e;
        }
    }

    public PostDTO getPostById(UUID postId) {
        try {
            Post post = postRepo.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

            // Increment view count
            post.setViews(post.getViews() + 1);
            postRepo.save(post);

            log.info("Post with id {} retrieved, views incremented to {}", postId, post.getViews());
            return mapPostToDTO(post);
        } catch (Exception e) {
            log.error("Error retrieving post by id {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    public PostDTO createPost(CreatePostRequest request) {
        try {
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
            log.info("Post '{}' created successfully by user {}", savedPost.getTitle(), currentUsername);
            return mapPostToDTO(savedPost);
        } catch (Exception e) {
            log.error("Error creating post: {}", e.getMessage());
            throw e;
        }
    }

    public PostDTO updatePost(UUID postId, UpdatePostRequest request) {
        try {
            Post post = postRepo.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

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
            log.info("Post with id {} updated successfully", postId);
            return mapPostToDTO(updatedPost);
        } catch (Exception e) {
            log.error("Error updating post with id {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    public void deletePost(UUID postId) {
        try {
            if (!postRepo.existsById(postId)) {
                throw new ResourceNotFoundException("Post not found with id: " + postId);
            }

            postRepo.deleteById(postId);
            log.info("Post with id {} deleted successfully", postId);
        } catch (Exception e) {
            log.error("Error deleting post with id {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    public Page<PostDTO> getPostsByAuthor(UUID authorId, Pageable pageable) {
        try {
            Page<PostDTO> posts = postRepo.findByAuthorId(authorId, pageable)
                    .map(this::mapPostToDTO);
            log.info("Retrieved {} posts by author {} (page {}, size {})", posts.getTotalElements(), authorId, pageable.getPageNumber(), pageable.getPageSize());
            return posts;
        } catch (Exception e) {
            log.error("Error retrieving posts by author {}: {}", authorId, e.getMessage());
            throw e;
        }
    }

    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        try {
            Page<PostDTO> posts = postRepo.findByTitleContaining(keyword, pageable)
                    .map(this::mapPostToDTO);
            log.info("Searched posts with keyword '{}' - found {} results (page {}, size {})", keyword, posts.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize());
            return posts;
        } catch (Exception e) {
            log.error("Error searching posts with keyword {}: {}", keyword, e.getMessage());
            throw e;
        }
    }

    public PostDTO publishPost(UUID postId) {
        try {
            Post post = postRepo.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

            post.setPublished(true);
            post.setUpdatedAt(Instant.now());
            Post updatedPost = postRepo.save(post);
            log.info("Post with id {} published successfully", postId);
            return mapPostToDTO(updatedPost);
        } catch (Exception e) {
            log.error("Error publishing post with id {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    public PostDTO unpublishPost(UUID postId) {
        try {
            Post post = postRepo.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

            post.setPublished(false);
            post.setUpdatedAt(Instant.now());
            Post updatedPost = postRepo.save(post);
            log.info("Post with id {} unpublished successfully", postId);
            return mapPostToDTO(updatedPost);
        } catch (Exception e) {
            log.error("Error unpublishing post with id {}: {}", postId, e.getMessage());
            throw e;
        }
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
