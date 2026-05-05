package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.commentDTO.CommentDTO;
import com.example.BlogApp.DTO.commentDTO.CreateCommentRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.GlobalExceptionHandler;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.service.CommentService;
import com.example.BlogApp.utils.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentController Tests")
public class CommentControllerTest {
    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;

    private UUID authorId;
    private UUID postId;
    private UUID commentId;
    private UserDTO testUserDTO;
    private CommentDTO testCommentDTO;
    private CreateCommentRequest createCommentRequest;
    private static final Instant randInstant = Instant.parse("2024-01-01T00:00:00Z");

    @BeforeEach
    void setup() throws Exception {
        try (@SuppressWarnings("unused") var autoCloseable = MockitoAnnotations.openMocks(this)) {
            mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
        }

        authorId = UUID.randomUUID();
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        testUserDTO = UserDTO.builder()
                .id(authorId)
                .username("testuser")
                .email("test@example.com")
                .createdAt(randInstant)
                .build();

        testCommentDTO = CommentDTO.builder()
                .id(commentId)
                .content("This is a test comment")
                .author(testUserDTO)
                .postId(postId)
                .createdAt(randInstant)
                .build();

        createCommentRequest = CreateCommentRequest.builder()
                .content("This is a new test comment")
                .postId(postId)
                .build();
    }

    // ==================== GET COMMENTS BY POST TESTS ====================

    @Test
    @DisplayName("Should get all comments for post - Success")
    void testGetCommentsByPostSuccess() throws Exception {
        // Arrange
        List<CommentDTO> commentDTOList = List.of(testCommentDTO);
        when(commentService.getCommentsByPost(any())).thenReturn(commentDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Comments retrieved successfully"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$.data[0].content").value("This is a test comment"))
                .andExpect(jsonPath("$.data[0].author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.data[0].postId").value(postId.toString()));

        verify(commentService, times(1)).getCommentsByPost(any());
    }

    @Test
    @DisplayName("Should return empty list when no comments exist for post")
    void testGetCommentsByPostEmpty() throws Exception {
        // Arrange
        when(commentService.getCommentsByPost(any())).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(commentService, times(1)).getCommentsByPost(any());
    }

    @Test
    @DisplayName("Should return multiple comments for post")
    void testGetCommentsByPostMultiple() throws Exception {
        // Arrange
        UUID commentId2 = UUID.randomUUID();
        CommentDTO testCommentDTO2 = CommentDTO.builder()
                .id(commentId2)
                .content("This is another test comment")
                .author(testUserDTO)
                .postId(postId)
                .createdAt(randInstant)
                .build();

        List<CommentDTO> commentDTOList = List.of(testCommentDTO, testCommentDTO2);
        when(commentService.getCommentsByPost(any())).thenReturn(commentDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$.data[1].id").value(commentId2.toString()));

        verify(commentService, times(1)).getCommentsByPost(any());
    }

    // ==================== ADD COMMENT TESTS ====================

    @Test
    @DisplayName("Should add a comment to post - Success")
    void testAddCommentSuccess() throws Exception {
        // Arrange
        when(commentService.addComment(any())).thenReturn(testCommentDTO);

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(createCommentRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Comment added successfully"))
                .andExpect(jsonPath("$.data.id").value(commentId.toString()))
                .andExpect(jsonPath("$.data.content").value("This is a test comment"))
                .andExpect(jsonPath("$.data.author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.data.postId").value(postId.toString()));

        verify(commentService, times(1)).addComment(any());
    }

    @Test
    @DisplayName("Should throw exception for comment with blank content")
    void testAddCommentBlankContent() throws Exception {
        CreateCommentRequest blankContentRequest = CreateCommentRequest.builder()
                .content("")
                .postId(postId)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(blankContentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("The input data is invalid"))
                .andExpect(jsonPath("$.data.status").value(400));

        verify(commentService, never()).addComment(any());
    }

    @Test
    @DisplayName("Should add comment with missing content field")
    void testAddCommentMissingContent() throws Exception {
        // Arrange
        String jsonWithoutContent = "{\"postId\":\"" + postId + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutContent))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("The input data is invalid"))
                .andExpect(jsonPath("$.data.status").value(400));

        verify(commentService, never()).addComment(any());
    }

    @Test
    @DisplayName("Should add comment with empty JSON body")
    void testAddCommentEmptyBody() throws Exception {
        // Arrange
        String emptyJson = "{}";

        when(commentService.addComment(any())).thenReturn(testCommentDTO);

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("The input data is invalid"))
                .andExpect(jsonPath("$.data.status").value(400));

        verify(commentService, never()).addComment(any());
    }

    // ==================== UPDATE COMMENT TESTS ====================

    @Test
    @DisplayName("Should update a comment successfully")
    void testUpdateCommentSuccess() throws Exception {
        // Arrange
        CommentDTO updatedCommentDTO = CommentDTO.builder()
                .id(commentId)
                .content("This is an updated test comment")
                .author(testUserDTO)
                .postId(postId)
                .createdAt(randInstant)
                .build();

        when(commentService.updateComment(eq(commentId), any())).thenReturn(updatedCommentDTO);

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(createCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Comment updated successfully"))
                .andExpect(jsonPath("$.data.id").value(commentId.toString()))
                .andExpect(jsonPath("$.data.content").value("This is an updated test comment"));

        verify(commentService, times(1)).updateComment(eq(commentId), any());
    }

    @Test
    @DisplayName("Should update comment with blank content")
    void testUpdateCommentBlankContent() throws Exception {
        // Arrange
        CreateCommentRequest blankContentRequest = CreateCommentRequest.builder()
                .content("")
                .postId(postId)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(blankContentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("The input data is invalid"))
                .andExpect(jsonPath("$.data.status").value(400));

        verify(commentService, never()).updateComment(eq(commentId), any());
    }

    @Test
    @DisplayName("Should fail to update non-existent comment")
    void testUpdateCommentNotFound() throws Exception {
        // Arrange
        when(commentService.updateComment(eq(commentId), any()))
                .thenThrow(new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(createCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).updateComment(eq(commentId), any());
    }

    @Test
    @DisplayName("Should update comment with missing content field")
    void testUpdateCommentMissingContent() throws Exception {
        // Arrange
        String jsonWithoutContent = "{\"postId\":\"" + postId + "\"}";

        // Act & Assert
        mockMvc.perform(put("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutContent))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("The input data is invalid"))
                .andExpect(jsonPath("$.data.status").value(400));

        verify(commentService, never()).updateComment(eq(commentId), any());
    }

    // ==================== DELETE COMMENT TESTS ====================

    @Test
    @DisplayName("Should delete a comment successfully")
    void testDeleteCommentSuccess() throws Exception {
        // Arrange
        doNothing().when(commentService).deleteComment(commentId);

        // Act & Assert
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    @DisplayName("Should fail to delete non-existent comment")
    void testDeleteCommentNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Comment not found with id: " + commentId))
                .when(commentService).deleteComment(commentId);

        // Act & Assert
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).deleteComment(commentId);
    }

    // ==================== EDGE CASES AND ERROR HANDLING ====================

    @Test
    @DisplayName("Should handle invalid UUID format in path variable for GET")
    void testGetCommentsInvalidUUIDFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/posts/invalid-uuid/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle invalid UUID format in path variable for PUT")
    void testUpdateCommentInvalidUUIDFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/comments/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(createCommentRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle invalid UUID format in path variable for DELETE")
    void testDeleteCommentInvalidUUIDFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/comments/invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle invalid UUID format in path variable for POST")
    void testAddCommentInvalidUUIDFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/posts/invalid-uuid/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(createCommentRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
}
