package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.commentDTO.CommentDTO;
import com.example.BlogApp.DTO.commentDTO.CreateCommentRequest;
import com.example.BlogApp.service.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class CommentController {
    private CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<AuthResponse<CommentDTO>> addComment(@PathVariable UUID postId, @RequestBody CreateCommentRequest request) {
        try {
            // Set postId in request
            request.setPostId(postId);

            CommentDTO comment = commentService.addComment(request);

            AuthResponse<CommentDTO> response = new AuthResponse<>();
            response.setData(comment);
            response.setSuccess(true);
            response.setMessage("Comment added successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            AuthResponse<CommentDTO> response = new AuthResponse<>();
            response.setSuccess(false);
            response.setMessage("Comment creation failed: " + e.getMessage());
            response.setData(null);
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<AuthResponse<List<CommentDTO>>> getCommentsByPost(@PathVariable UUID postId) {
        try {
            List<CommentDTO> comments = commentService.getCommentsByPost(postId);

            AuthResponse<List<CommentDTO>> response = new AuthResponse<>();
            response.setData(comments);
            response.setSuccess(true);
            response.setMessage("Comments retrieved successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            AuthResponse<List<CommentDTO>> response = new AuthResponse<>();
            response.setSuccess(false);
            response.setMessage("Comments retrieval failed: " + e.getMessage());
            response.setData(null);
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.isCommentAuthor(#commentId)")
    public ResponseEntity<AuthResponse<CommentDTO>> updateComment(@PathVariable UUID commentId, @RequestBody CreateCommentRequest request) {
        try {
            CommentDTO updatedComment = commentService.updateComment(commentId, request);

            AuthResponse<CommentDTO> response = new AuthResponse<>();
            response.setData(updatedComment);
            response.setSuccess(true);
            response.setMessage("Comment updated successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            AuthResponse<CommentDTO> response = new AuthResponse<>();
            response.setSuccess(false);
            response.setMessage("Comment update failed: " + e.getMessage());
            response.setData(null);
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND :
                               HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.canDeleteComment(#commentId)")
    public ResponseEntity<AuthResponse<Void>> deleteComment(@PathVariable UUID commentId) {
        try {
            commentService.deleteComment(commentId);

            AuthResponse<Void> response = new AuthResponse<>();
            response.setSuccess(true);
            response.setMessage("Comment deleted successfully");
            response.setData(null);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            AuthResponse<Void> response = new AuthResponse<>();
            response.setSuccess(false);
            response.setMessage("Comment deletion failed: " + e.getMessage());
            response.setData(null);
            HttpStatus status = e.getMessage().contains("not found") ? HttpStatus.NOT_FOUND :
                               HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).body(response);
        }
    }
}
