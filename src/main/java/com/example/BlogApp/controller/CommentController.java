package com.example.BlogApp.controller;

import com.example.BlogApp.DTO.AuthResponse;
import com.example.BlogApp.DTO.commentDTO.CommentDTO;
import com.example.BlogApp.DTO.commentDTO.CreateCommentRequest;
import com.example.BlogApp.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Comments", description = "Operations related to comments on blog posts")
public class CommentController {
    private CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "Add a comment to a post", description = "Adds a new comment to the specified post.")
    public ResponseEntity<AuthResponse<CommentDTO>> addComment(@PathVariable UUID postId, @RequestBody CreateCommentRequest request) {
        // Set postId in request
        request.setPostId(postId);

        CommentDTO comment = commentService.addComment(request);

        AuthResponse<CommentDTO> response = new AuthResponse<>();
        response.setData(comment);
        response.setSuccess(true);
        response.setMessage("Comment added successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "Get comments for a post", description = "Retrieves all comments associated with the specified post.")
    public ResponseEntity<AuthResponse<List<CommentDTO>>> getCommentsByPost(@PathVariable UUID postId) {
            List<CommentDTO> comments = commentService.getCommentsByPost(postId);

            AuthResponse<List<CommentDTO>> response = new AuthResponse<>();
            response.setData(comments);
            response.setSuccess(true);
            response.setMessage("Comments retrieved successfully");
            return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.isCommentAuthor(#commentId)")
    @Operation(summary = "Update a comment", description = "Updates the content of an existing comment. Only the author of the comment can perform this action.")
    public ResponseEntity<AuthResponse<CommentDTO>> updateComment(@PathVariable UUID commentId, @RequestBody CreateCommentRequest request) {
        CommentDTO updatedComment = commentService.updateComment(commentId, request);

        AuthResponse<CommentDTO> response = new AuthResponse<>();
        response.setData(updatedComment);
        response.setSuccess(true);
        response.setMessage("Comment updated successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("@securityService.canDeleteComment(#commentId)")
    @Operation(summary = "Delete a comment", description = "Deletes an existing comment. Only the author of the comment or an admin can perform this action.")
    public ResponseEntity<AuthResponse<Void>> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);

        AuthResponse<Void> response = new AuthResponse<>();
        response.setSuccess(true);
        response.setMessage("Comment deleted successfully");
        response.setData(null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
