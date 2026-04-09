package com.example.BlogApp.service;

import com.example.BlogApp.DTO.commentDTO.CommentDTO;
import com.example.BlogApp.DTO.commentDTO.CreateCommentRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.Comment;
import com.example.BlogApp.repo.CommentRepo;
import com.example.BlogApp.repo.PostRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CommentService {
    private CommentRepo commentRepo;
    private PostRepo postRepo;
    private UserService userService;

    public CommentDTO addComment(CreateCommentRequest request) {
        try {
            // Verify post exists
            if (!postRepo.existsById(request.getPostId())) {
                throw new ResourceNotFoundException("Post not found with id: " + request.getPostId());
            }

            String currentUsername = getCurrentUsername();
            UserDTO currentUser = userService.getUserByUsername(currentUsername);

            Comment comment = new Comment();
            comment.setContent(request.getContent());
            comment.setAuthorId(currentUser.getId());
            comment.setPostId(request.getPostId());

            Comment savedComment = commentRepo.save(comment);
            return mapCommentToDTO(savedComment);
        } catch (Exception e) {
            log.error("Error adding comment to post {}: {}", request.getPostId(), e.getMessage());
            throw e;
        }
    }

    public List<CommentDTO> getCommentsByPost(UUID postId) {
        try {
            // Verify post exists
            if (!postRepo.existsById(postId)) {
                throw new ResourceNotFoundException("Post not found with id: " + postId);
            }

            return commentRepo.findByPostId(postId).stream()
                    .map(this::mapCommentToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving comments for post {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    public CommentDTO updateComment(UUID commentId, CreateCommentRequest request) {
        try {
            Comment comment = commentRepo.findById(commentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

            comment.setContent(request.getContent());
            Comment updatedComment = commentRepo.save(comment);
            return mapCommentToDTO(updatedComment);
        } catch (Exception e) {
            log.error("Error updating comment with id {}: {}", commentId, e.getMessage());
            throw e;
        }
    }

    public void deleteComment(UUID commentId) {
        try {
            if (!commentRepo.existsById(commentId)) {
                throw new ResourceNotFoundException("Comment not found with id: " + commentId);
            }
            commentRepo.deleteById(commentId);
        } catch (Exception e) {
            log.error("Error deleting comment with id {}: {}", commentId, e.getMessage());
            throw e;
        }
    }

    private CommentDTO mapCommentToDTO(Comment comment) {
        UserDTO authorDTO = userService.getUserById(comment.getAuthorId());

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getContent());
        commentDTO.setAuthor(authorDTO);
        commentDTO.setPostId(comment.getPostId());
        commentDTO.setCreatedAt(comment.getCreatedAt());
        return commentDTO;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        return authentication.getName();
    }
}
