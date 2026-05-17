package com.example.BlogApp.service;

import com.example.BlogApp.DTO.postDTO.CreatePostRequest;
import com.example.BlogApp.DTO.postDTO.PostDTO;
import com.example.BlogApp.DTO.postDTO.UpdatePostRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.Post;
import com.example.BlogApp.repo.PostRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PostService
 * Tests blog post management, publishing, searching, and view counting
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepo postRepo;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private UUID testPostId;
    private UUID testAuthorId;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testPostId = UUID.randomUUID();
        testAuthorId = UUID.randomUUID();

        testPost = new Post();
        testPost.setId(testPostId);
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");
        testPost.setAuthorId(testAuthorId);
        testPost.setViews(0);
        testPost.setPublished(false);
        testPost.setCreatedAt(Instant.now());
        testPost.setUpdatedAt(Instant.now());

        testUserDTO = UserDTO.builder()
                .id(testAuthorId)
                .username("testuser")
                .email("testuser@example.com")
                .build();
    }

    // ==================== Get Posts Tests ====================

    @Test
    void testGetAllPosts_Success() {
        // Arrange
        Page<Post> postsPage = new PageImpl<>(Collections.singletonList(testPost));
        when(postRepo.findByPublishedTrue(any(Pageable.class))).thenReturn(postsPage);

        // Act
        Page<PostDTO> result = postService.getAllPosts(any(Pageable.class));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetPostById_IncrementsViewCount() {
        // Arrange
        testPost.setViews(5);
        when(postRepo.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        PostDTO result = postService.getPostById(testPostId);

        // Assert
        assertNotNull(result);
        assertEquals(6, testPost.getViews());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void testGetPostById_PostNotFound_ThrowsException() {
        // Arrange
        when(postRepo.findById(testPostId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(testPostId));
    }

    // ==================== Create Post Tests ====================

    @Test
    void testCreatePost_Success() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New content");
        request.setTags(Arrays.asList("tag1", "tag2"));

        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(testPostId);
            return post;
        });

        // Act
        PostDTO result = postService.createPost(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.getTitle(), result.getTitle());
        assertFalse(result.isPublished());
        assertEquals(0, result.getViews());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void testCreatePost_UnpublishedByDefault() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("Content");
        request.setTags(List.of());

        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(testPostId);
            return post;
        });

        // Act
        PostDTO result = postService.createPost(request);

        // Assert
        assertFalse(result.isPublished());
    }

    // ==================== Update Post Tests ====================

    @Test
    void testUpdatePost_UpdateTitle_Success() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Title");

        when(postRepo.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        PostDTO result = postService.updatePost(testPostId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void testUpdatePost_UpdateContent_Success() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest();
        request.setContent("Updated content");

        when(postRepo.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        PostDTO result = postService.updatePost(testPostId, request);

        // Assert
        assertEquals("Updated content", result.getContent());
    }

    @Test
    void testUpdatePost_PostNotFound_ThrowsException() {
        // Arrange
        UpdatePostRequest request = new UpdatePostRequest();
        when(postRepo.findById(testPostId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> postService.updatePost(testPostId, request));
    }

    // ==================== Delete Post Tests ====================

    @Test
    void testDeletePost_Success() {
        // Arrange
        when(postRepo.existsById(testPostId)).thenReturn(true);

        // Act
        postService.deletePost(testPostId);

        // Assert
        verify(postRepo, times(1)).deleteById(testPostId);
    }

    @Test
    void testDeletePost_PostNotFound_ThrowsException() {
        // Arrange
        when(postRepo.existsById(testPostId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> postService.deletePost(testPostId));
    }

    // ==================== Publish/Unpublish Tests ====================

    @Test
    void testPublishPost_Success() {
        // Arrange
        testPost.setPublished(false);
        when(postRepo.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        PostDTO result = postService.publishPost(testPostId);

        // Assert
        assertTrue(result.isPublished());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void testUnpublishPost_Success() {
        // Arrange
        testPost.setPublished(true);
        when(postRepo.findById(testPostId)).thenReturn(Optional.of(testPost));
        when(postRepo.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        PostDTO result = postService.unpublishPost(testPostId);

        // Assert
        assertFalse(result.isPublished());
        verify(postRepo, times(1)).save(any(Post.class));
    }

    @Test
    void testPublishPost_PostNotFound_ThrowsException() {
        // Arrange
        when(postRepo.findById(testPostId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> postService.publishPost(testPostId));
    }

    // ==================== Search Tests ====================

    @Test
    void testSearchPosts_Success() {
        // Arrange
        Page<Post> searchResults = new PageImpl<>(Arrays.asList(testPost));
        when(postRepo.findByTitleContainingOrContentContaining("test", "test", any(Pageable.class)))
                .thenReturn(searchResults);

        // Act
        Page<PostDTO> result = postService.searchPosts("test", any(Pageable.class));

        // Assert
        assertNotNull(result);
        verify(postRepo, times(1)).findByTitleContainingOrContentContaining(anyString(), anyString(), any(Pageable.class));
    }

    // ==================== Get Posts By Author Tests ====================

    @Test
    void testGetPostsByAuthor_AuthorViewingOwnPosts() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Page<Post> authorPosts = new PageImpl<>(Arrays.asList(testPost));
        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);
        when(postRepo.findByAuthorId(testAuthorId, any(Pageable.class)))
                .thenReturn(authorPosts);

        // Act
        Page<PostDTO> result = postService.getPostsByAuthor(testAuthorId, any(Pageable.class));

        // Assert
        assertNotNull(result);
        verify(postRepo, times(1)).findByAuthorId(testAuthorId, any(Pageable.class));
    }
}

