package com.prettyshopbe.prettyshopbe.controller;

import com.prettyshopbe.prettyshopbe.model.Comment;
import com.prettyshopbe.prettyshopbe.model.Product;
import com.prettyshopbe.prettyshopbe.service.AuthenticationService;
import com.prettyshopbe.prettyshopbe.service.CommentService;
import com.prettyshopbe.prettyshopbe.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webjars.NotFoundException;

import java.util.List;

@RestController
@RequestMapping("/products/{productId}/comments")
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    ProductService productService;

    @Autowired
    AuthenticationService authenticationService;

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getComment(@PathVariable("productId") Integer productId,
                                              @PathVariable("id") Long commentId,
                                              @RequestParam(value = "token", required = false) String token) throws NotFoundException {
        Product product = productService.getProductById(productId);
        Comment comment = commentService.getComment(commentId, authenticationService.getUser(token).getId());
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/")
    public ResponseEntity<List<Comment>> getComments(@PathVariable("productId") Integer productId) {
        List<Comment> comments = commentService.getCommentsByProductId(productId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("")
    public ResponseEntity<Comment> createComment(@PathVariable("productId") Integer productId,
                                                 @RequestBody Comment comment,
                                                 @RequestParam(value = "token", required = true) String token) throws NotFoundException {
        Product product = productService.getProductById(productId);
        comment.setProduct(product);
        comment.setUser(authenticationService.getUser(token)); // Gán người tạo bình luận cho người đang đăng nhập
        Comment newComment = commentService.createComment(comment);
        return ResponseEntity.ok(newComment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable("productId") Integer productId,
                                                 @PathVariable("id") Long commentId,
                                                 @RequestBody Comment comment,
                                                 @RequestParam(value = "token", required = true) String token) throws NotFoundException {
        Product product = productService.getProductById(productId);
        Comment updatedComment = commentService.updateComment(commentId, comment, authenticationService.getUser(token).getId());
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Comment> deleteComment(@PathVariable("productId") Integer productId,
                                                 @PathVariable("id") Long commentId,
                                                 @RequestParam(value = "token", required = true) String token) throws NotFoundException {
        try {
            commentService.deleteComment(commentId, authenticationService.getUser(token).getId());
            return ResponseEntity.noContent().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }

    }
}