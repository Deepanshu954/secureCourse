package com.securecourse.backend.config;

import com.securecourse.backend.auth.AuthService;
import com.securecourse.backend.course.Course;
import com.securecourse.backend.course.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(CourseRepository courseRepository, AuthService authService) {
        return args -> {
            if (courseRepository.count() == 0) {
                Course course = new Course();
                course.setTitle("Cybersecurity 101");
                course.setDescription("Introduction to Web Security");
                courseRepository.save(course);
            }
            
            try {
                authService.signup("admin", "password");
            } catch (Exception e) {
                // User might already exist
            }
        };
    }
}
