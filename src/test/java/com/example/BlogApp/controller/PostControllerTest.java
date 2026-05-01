package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.postDTO.CreatePostRequest;
import com.example.BlogApp.DTO.postDTO.PostDTO;
import com.example.BlogApp.DTO.postDTO.UpdatePostRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.GlobalExceptionHandler;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostController Tests")
class PostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private ObjectMapper objectMapper;

    private PostDTO testPostDTO;
    private CreatePostRequest createPostRequest;
    private UpdatePostRequest updatePostRequest;
    private UserDTO testUserDTO;
    private UUID postId;
    private UUID authorId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        postId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);

        // Setup test user DTO
        testUserDTO = UserDTO.builder()
                .id(authorId)
                .username("testuser")
                .email("test@example.com")
                .createdAt(Instant.now())
                .build();

        // Setup test post DTO
        testPostDTO = PostDTO.builder()
                .id(postId)
                .title("Test Post Title")
                .content("This is a test post content with more than 10 characters")
                .author(testUserDTO)
                .tags(Arrays.asList("java", "spring"))
                .views(5L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Setup create post request
        createPostRequest = CreatePostRequest.builder()
                .title("New Test Post")
                .content("This is a new test post with more than 10 characters")
                .tags(Arrays.asList("testing", "mockito"))
                .build();

        // Setup update post request
        updatePostRequest = UpdatePostRequest.builder()
                .id(postId)
                .title("Updated Test Post")
                .content("This is an updated test post content with more than 10 characters")
                .tags(Arrays.asList("updated", "test"))
                .build();
    }

    // ==================== GET ALL POSTS TESTS ====================

    @Test
    @DisplayName("Should get all posts successfully")
    void testGetAllPostsSuccess() throws Exception {
        // Arrange
        List<PostDTO> postList = Arrays.asList(testPostDTO);
        Page<PostDTO> page = new PageImpl<>(postList, pageable, 1);
        when(postService.getAllPosts(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Posts retrieved successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(postId.toString()))
                .andExpect(jsonPath("$.data.content[0].title").value("Test Post Title"));

        verify(postService, times(1)).getAllPosts(any());
    }

    @Test
    @DisplayName("Should get all posts with default pagination parameters")
    void testGetAllPostsWithDefaultParams() throws Exception {
        // Arrange
        Page<PostDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(postService.getAllPosts(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)));

        verify(postService, times(1)).getAllPosts(any());
    }

    // ==================== GET POST BY ID TESTS ====================

    @Test
    @DisplayName("Should get post by ID successfully")
    void testGetPostByIdSuccess() throws Exception {
        // Arrange
        when(postService.getPostById(postId)).thenReturn(testPostDTO);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Post retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()))
                .andExpect(jsonPath("$.data.title").value("Test Post Title"))
                .andExpect(jsonPath("$.data.content").value("This is a test post content with more than 10 characters"))
                .andExpect(jsonPath("$.data.author.username").value("testuser"));

        verify(postService, times(1)).getPostById(postId);
    }

    @Test
    @DisplayName("Should return 500 when post not found by ID")
    void testGetPostByIdNotFound() throws Exception {
        // Arrange
        when(postService.getPostById(postId))
                .thenThrow(new ResourceNotFoundException("Post not found with id: " + postId));

        // Act & Assert
        mockMvc.perform(get("/api/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(postService, times(1)).getPostById(postId);
    }

    // ==================== CREATE POST TESTS ====================

    @Test
    @DisplayName("Should create a new post successfully")
    void testCreatePostSuccess() throws Exception {
        // Arrange
        when(postService.createPost(any())).thenReturn(testPostDTO);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Post created successfully"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()))
                .andExpect(jsonPath("$.data.title").value("Test Post Title"));

        verify(postService, times(1)).createPost(any());
    }

    @Test
    @DisplayName("Should fail to create post with invalid request - blank title")
    void testCreatePostInvalidTitle() throws Exception {
        // Arrange
        CreatePostRequest invalidRequest = CreatePostRequest.builder()
                .title("")
                .content("Valid content with more than 10 characters")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any());
    }

    @Test
    @DisplayName("Should fail to create post with invalid request - short content")
    void testCreatePostShortContent() throws Exception {
        // Arrange
        CreatePostRequest invalidRequest = CreatePostRequest.builder()
                .title("Valid Title")
                .content("short")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any());
    }

    @Test
    @DisplayName("Should fail to create post with missing required fields")
    void testCreatePostMissingFields() throws Exception {
        // Arrange
        String invalidJson = "{}";

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any());
    }

    // ==================== UPDATE POST TESTS ====================

    @Test
    @DisplayName("Should update post successfully")
    void testUpdatePostSuccess() throws Exception {
        // Arrange
        when(postService.updatePost(eq(postId), any()))
                .thenReturn(testPostDTO);

        // Act & Assert
        mockMvc.perform(put("/api/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePostRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Post updated successfully"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()));

        verify(postService, times(1)).updatePost(eq(postId), any());
    }

    @Test
    @DisplayName("Should fail to update post with invalid request")
    void testUpdatePostInvalid() throws Exception {
        // Arrange
        UpdatePostRequest invalidRequest = UpdatePostRequest.builder()
                .title("")
                .content("short")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(postService, never()).updatePost(any(), any());
    }

    // ==================== DELETE POST TESTS ====================

    @Test
    @DisplayName("Should delete post successfully")
    void testDeletePostSuccess() throws Exception {
        // Arrange
        doNothing().when(postService).deletePost(postId);

        // Act & Assert
        mockMvc.perform(delete("/api/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Post deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(postService, times(1)).deletePost(postId);
    }

    @Test
    @DisplayName("Should fail to delete non-existent post")
    void testDeletePostNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Post not found with id: " + postId))
                .when(postService).deletePost(postId);

        // Act & Assert
        mockMvc.perform(delete("/api/posts/{postId}", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(postService, times(1)).deletePost(postId);
    }

    // ==================== GET POSTS BY AUTHOR TESTS ====================

    @Test
    @DisplayName("Should get posts by author successfully")
    void testGetPostsByAuthorSuccess() throws Exception {
        // Arrange
        List<PostDTO> authorPosts = Arrays.asList(testPostDTO);
        Page<PostDTO> page = new PageImpl<>(authorPosts, pageable, 1);
        when(postService.getPostsByAuthor(eq(authorId), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/posts/author/{authorId}", authorId)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Posts by author retrieved successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].author.id").value(authorId.toString()));

        verify(postService, times(1)).getPostsByAuthor(eq(authorId), any());
    }

    @Test
    @DisplayName("Should get empty page for author with no posts")
    void testGetPostsByAuthorEmpty() throws Exception {
        // Arrange
        Page<PostDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(postService.getPostsByAuthor(eq(authorId), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts/author/{authorId}", authorId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)));

        verify(postService, times(1)).getPostsByAuthor(eq(authorId), any());
    }

    // ==================== SEARCH POSTS TESTS ====================

    @Test
    @DisplayName("Should search posts successfully")
    void testSearchPostsSuccess() throws Exception {
        // Arrange
        String keyword = "Spring";
        List<PostDTO> searchResults = Arrays.asList(testPostDTO);
        Page<PostDTO> page = new PageImpl<>(searchResults, pageable, 1);
        when(postService.searchPosts(eq(keyword), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/posts/search")
                .param("keyword", keyword)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Posts searched successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)));

        verify(postService, times(1)).searchPosts(eq(keyword), any());
    }

    @Test
    @DisplayName("Should search posts with no results")
    void testSearchPostsNoResults() throws Exception {
        // Arrange
        String keyword = "nonexistent";
        Page<PostDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(postService.searchPosts(eq(keyword), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts/search")
                .param("keyword", keyword)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)));

        verify(postService, times(1)).searchPosts(eq(keyword), any());
    }

    @Test
    @DisplayName("Should fetch all posts without keyword parameter")
    void testSearchPostsMissingKeyword() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/posts/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Posts searched successfully"));

        verify(postService, times(1)).searchPosts(anyString(), any());
    }

    // ==================== PUBLISH/UNPUBLISH POST TESTS ====================

    @Test
    @DisplayName("Should publish post successfully")
    void testPublishPostSuccess() throws Exception {
        // Arrange
        PostDTO publishedPost = PostDTO.builder()
                .id(postId)
                .title("Test Post Title")
                .content("This is a test post content with more than 10 characters")
                .author(testUserDTO)
                .tags(Arrays.asList("java", "spring"))
                .views(5L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(postService.publishPost(postId)).thenReturn(publishedPost);

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/publish", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Post published successfully"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()));

        verify(postService, times(1)).publishPost(postId);
    }

    @Test
    @DisplayName("Should fail to publish non-existent post")
    void testPublishPostNotFound() throws Exception {
        // Arrange
        when(postService.publishPost(postId))
                .thenThrow(new ResourceNotFoundException("Post not found with id: " + postId));

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/publish", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(postService, times(1)).publishPost(postId);
    }

    @Test
    @DisplayName("Should unpublish post successfully")
    void testUnpublishPostSuccess() throws Exception {
        // Arrange
        PostDTO unpublishedPost = PostDTO.builder()
                .id(postId)
                .title("Test Post Title")
                .content("This is a test post content with more than 10 characters")
                .author(testUserDTO)
                .tags(Arrays.asList("java", "spring"))
                .views(5L)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(postService.unpublishPost(postId)).thenReturn(unpublishedPost);

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/unpublish", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Post unpublished successfully"))
                .andExpect(jsonPath("$.data.id").value(postId.toString()));

        verify(postService, times(1)).unpublishPost(postId);
    }

    @Test
    @DisplayName("Should fail to unpublish non-existent post")
    void testUnpublishPostNotFound() throws Exception {
        // Arrange
        when(postService.unpublishPost(postId))
                .thenThrow(new ResourceNotFoundException("Post not found with id: " + postId));

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/unpublish", postId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(postService, times(1)).unpublishPost(postId);
    }

    // ==================== EDGE CASES AND ERROR HANDLING ====================

    @Test
    @DisplayName("Should handle invalid UUID format in path variable")
    void testInvalidUUIDFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/posts/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle negative pagination page number")
    void testNegativePaginationPage() throws Exception {
        // Arrange - pagination with page -1 should result in a server error due to validation
        // Act & Assert
        mockMvc.perform(get("/api/posts")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle large pagination size")
    void testLargePaginationSize() throws Exception {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(List.of(), pageRequestWithSize(1000), 0);
        when(postService.getAllPosts(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/posts")
                .param("page", "0")
                .param("size", "1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== HELPER METHODS ====================

    private Pageable pageRequestWithSize(int size) {
        return PageRequest.of(0, size);
    }
}























