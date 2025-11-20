package com.securecourse.backend.comments;

import com.securecourse.backend.course.Course;
import com.securecourse.backend.course.CourseRepository; // Assuming this is public or I need to move it
import com.securecourse.backend.toggles.ToggleService;
import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository; // Need to make sure CourseRepository is accessible
    private final ToggleService toggleService;

    public CommentService(CommentRepository commentRepository, CourseRepository courseRepository, ToggleService toggleService) {
        this.commentRepository = commentRepository;
        this.courseRepository = courseRepository; // This might fail if CourseRepository is package-private in another package
        this.toggleService = toggleService;
    }

    public Comment addComment(Long courseId, String content, String author) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));
        
        String finalContent;
        if (toggleService.isXssProtectionEnabled()) {
            // SAFE MODE: Encode HTML
            finalContent = Encode.forHtml(content);
        } else {
            // UNSAFE MODE: Raw HTML (Stored XSS)
            finalContent = content;
        }

        Comment comment = new Comment();
        comment.setContent(finalContent);
        comment.setAuthor(author);
        comment.setCourse(course);
        
        return commentRepository.save(comment);
    }
}
