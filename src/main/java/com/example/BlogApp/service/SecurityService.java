package com.example.BlogApp.service;

import com.example.BlogApp.DTO.userDTO.UserDTO;
import com.example.BlogApp.exception.ResourceNotFoundException;
import com.example.BlogApp.model.Comment;
import com.example.BlogApp.model.Post;
import com.example.BlogApp.model.User;
import com.example.BlogApp.repo.CommentRepo;
import com.example.BlogApp.repo.PostRepo;
import com.example.BlogApp.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class SecurityService {
    private PostRepo postRepo;
    private CommentRepo commentRepo;
    private UserRepo userRepo;
    private UserService userService;

    /**
     * Check if current user is the author of a post
     */
    public boolean isPostAuthor(UUID postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        UUID currentUserId = getCurrentUserId();
        return post.getAuthorId().equals(currentUserId);
    }

    /**
     * Check if current user is the author of a comment
     */
    public boolean isCommentAuthor(UUID commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        UUID currentUserId = getCurrentUserId();
        return comment.getAuthorId().equals(currentUserId);
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        String username = getCurrentUsername();
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        return user.getRoles() != null && user.getRoles().contains("ADMIN");
    }

    /**
     * Check if current user can delete post (is author or admin)
     */
    public boolean canDeletePost(UUID postId) {
        return isPostAuthor(postId) || isAdmin();
    }

    /**
     * Check if current user can delete comment (is author or admin)
     */
    public boolean canDeleteComment(UUID commentId) {
        return isCommentAuthor(commentId) || isAdmin();
    }

    /**
     * Check if current user can view an unpublished post (is author or admin)
     */
    public boolean canViewUnpublishedPost(UUID postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if(!post.isPublished()) {
            UUID currentUser = getCurrentUserId();
            return currentUser == post.getAuthorId() || isAdmin();
        }
        return true;
    }

    private UUID getCurrentUserId() {
        String username = getCurrentUsername();
        UserDTO userDTO = userService.getUserByUsername(username);
        return userDTO.getId();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        return authentication.getName();
    }
}






