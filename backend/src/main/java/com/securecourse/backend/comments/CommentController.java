package com.securecourse.backend.comments;

import com.securecourse.backend.course.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/course")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CommentController {

    private final CommentService commentService;
    private final CourseRepository courseRepository;
    private final CommentRepository commentRepository;

    public CommentController(CommentService commentService, CourseRepository courseRepository,
            CommentRepository commentRepository) {
        this.commentService = commentService;
        this.courseRepository = courseRepository;
        this.commentRepository = commentRepository;
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String content = payload.get("content");
            String author = payload.get("author"); // In real app, get from session
            if (author == null)
                author = "Anonymous";

            Comment comment = commentService.addComment(id, content, author);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/comment")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentRepository.findByCourseId(id));
    }

    @DeleteMapping("/{courseId}/comments/reset")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> resetComments(@PathVariable Long courseId) {
        commentRepository.deleteByCourseId(courseId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Comments reset"));
    }
}
