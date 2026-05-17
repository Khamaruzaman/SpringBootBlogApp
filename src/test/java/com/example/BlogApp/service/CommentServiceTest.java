package com.example.BlogApp.service;

import com.example.BlogApp.DTO.commentDTO.CommentDTO;
import com.example.BlogApp.DTO.commentDTO.CreateCommentRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.Comment;
import com.example.BlogApp.repo.CommentRepo;
import com.example.BlogApp.repo.PostRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CommentService
 * Tests comment operations, post validation, and authorization
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepo commentRepo;

    @Mock
    private PostRepo postRepo;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private UUID testCommentId;
    private UUID testPostId;
    private UUID testAuthorId;
    private UserDTO testUserDTO;
    private CreateCommentRequest createCommentRequest;

    @BeforeEach
    void setUp() {
        testCommentId = UUID.randomUUID();
        testPostId = UUID.randomUUID();
        testAuthorId = UUID.randomUUID();

        testComment = new Comment();
        testComment.setId(testCommentId);
        testComment.setContent("Test comment");
        testComment.setPostId(testPostId);
        testComment.setAuthorId(testAuthorId);
        testComment.setCreatedAt(Instant.now());

        testUserDTO = UserDTO.builder()
                .id(testAuthorId)
                .username("testuser")
                .email("testuser@example.com")
                .build();

        createCommentRequest = new CreateCommentRequest();
        createCommentRequest.setContent("New comment");
        createCommentRequest.setPostId(testPostId);
    }

    // ==================== Add Comment Tests ====================

    @Test
    void testAddComment_Success() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(postRepo.existsById(testPostId)).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);
        when(commentRepo.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(testCommentId);
            return comment;
        });

        // Act
        CommentDTO result = commentService.addComment(createCommentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(createCommentRequest.getContent(), result.getContent());
        assertEquals(testPostId, result.getPostId());
        verify(commentRepo, times(1)).save(any(Comment.class));
    }

    @Test
    void testAddComment_PostNotFound_ThrowsException() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(postRepo.existsById(testPostId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> commentService.addComment(createCommentRequest));
        verify(commentRepo, never()).save(any(Comment.class));
    }

    @Test
    void testAddComment_AuthorAssignedToCurrentUser() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(postRepo.existsById(testPostId)).thenReturn(true);
        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);
        when(commentRepo.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(testCommentId);
            return comment;
        });

        // Act
        CommentDTO result = commentService.addComment(createCommentRequest);

        // Assert
        assertEquals(testUserDTO.getId(), result.getAuthor().getId());
        verify(userService, times(1)).getUserByUsername("testuser");
    }

    // ==================== Get Comments By Post Tests ====================

    @Test
    void testGetCommentsByPost_Success() {
        // Arrange
        List<Comment> comments = Arrays.asList(testComment);
        when(postRepo.existsById(testPostId)).thenReturn(true);
        when(commentRepo.findByPostId(testPostId)).thenReturn(comments);
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        List<CommentDTO> result = commentService.getCommentsByPost(testPostId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getContent(), result.get(0).getContent());
        verify(commentRepo, times(1)).findByPostId(testPostId);
    }

    @Test
    void testGetCommentsByPost_PostNotFound_ThrowsException() {
        // Arrange
        when(postRepo.existsById(testPostId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentsByPost(testPostId));
        verify(commentRepo, never()).findByPostId(any(UUID.class));
    }

    @Test
    void testGetCommentsByPost_EmptyComments() {
        // Arrange
        when(postRepo.existsById(testPostId)).thenReturn(true);
        when(commentRepo.findByPostId(testPostId)).thenReturn(Arrays.asList());

        // Act
        List<CommentDTO> result = commentService.getCommentsByPost(testPostId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetCommentsByPost_MultipleComments() {
        // Arrange
        Comment comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setContent("Another comment");
        comment2.setPostId(testPostId);
        comment2.setAuthorId(testAuthorId);

        List<Comment> comments = Arrays.asList(testComment, comment2);
        when(postRepo.existsById(testPostId)).thenReturn(true);
        when(commentRepo.findByPostId(testPostId)).thenReturn(comments);
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        List<CommentDTO> result = commentService.getCommentsByPost(testPostId);

        // Assert
        assertEquals(2, result.size());
    }

    // ==================== Update Comment Tests ====================

    @Test
    void testUpdateComment_Success() {
        // Arrange
        CreateCommentRequest updateRequest = new CreateCommentRequest();
        updateRequest.setContent("Updated comment content");

        when(commentRepo.findById(testCommentId)).thenReturn(Optional.of(testComment));
        when(commentRepo.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userService.getUserById(testAuthorId)).thenReturn(testUserDTO);

        // Act
        CommentDTO result = commentService.updateComment(testCommentId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated comment content", result.getContent());
        verify(commentRepo, times(1)).save(any(Comment.class));
    }

    @Test
    void testUpdateComment_CommentNotFound_ThrowsException() {
        // Arrange
        CreateCommentRequest updateRequest = new CreateCommentRequest();
        updateRequest.setContent("Updated content");

        when(commentRepo.findById(testCommentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(testCommentId, updateRequest));
        verify(commentRepo, never()).save(any(Comment.class));
    }

    // ==================== Delete Comment Tests ====================

    @Test
    void testDeleteComment_Success() {
        // Arrange
        when(commentRepo.existsById(testCommentId)).thenReturn(true);

        // Act
        commentService.deleteComment(testCommentId);

        // Assert
        verify(commentRepo, times(1)).deleteById(testCommentId);
    }

    @Test
    void testDeleteComment_CommentNotFound_ThrowsException() {
        // Arrange
        when(commentRepo.existsById(testCommentId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(testCommentId));
        verify(commentRepo, never()).deleteById(any(UUID.class));
    }

    @Test
    void testDeleteComment_VerifyDeleteCalled() {
        // Arrange
        when(commentRepo.existsById(testCommentId)).thenReturn(true);

        // Act
        commentService.deleteComment(testCommentId);

        // Assert
        verify(commentRepo, times(1)).existsById(testCommentId);
        verify(commentRepo, times(1)).deleteById(testCommentId);
    }
}

