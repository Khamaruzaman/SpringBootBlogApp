package com.example.BlogApp.service;

import com.example.BlogApp.DTO.commentDTO.CommentDTO;
import com.example.BlogApp.DTO.commentDTO.CreateCommentRequest;
import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.Comment;
import com.example.BlogApp.repo.CommentRepo;
import com.example.BlogApp.repo.PostRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentService {
    private CommentRepo commentRepo;
    private PostRepo postRepo;
    private UserService userService;

    public CommentDTO addComment(CreateCommentRequest request) {
        // Verify post exists
        postRepo.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + request.getPostId()));

        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthorId(currentUser.getId());
        comment.setPostId(request.getPostId());

        Comment savedComment = commentRepo.save(comment);
        return mapCommentToDTO(savedComment);
    }

    public List<CommentDTO> getCommentsByPost(UUID postId) {
        // Verify post exists
        if (!postRepo.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        return commentRepo.findByPostId(postId).stream()
                .map(this::mapCommentToDTO)
                .collect(Collectors.toList());
    }

    public CommentDTO updateComment(UUID commentId, CreateCommentRequest request) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Check if current user is the author
        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);
        if (!comment.getAuthorId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own comments");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepo.save(comment);
        return mapCommentToDTO(updatedComment);
    }

    public void deleteComment(UUID commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Check if current user is the author
        String currentUsername = getCurrentUsername();
        UserDTO currentUser = userService.getUserByUsername(currentUsername);
        if (!comment.getAuthorId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only delete your own comments");
        }

        commentRepo.deleteById(commentId);
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
