package com.securecourse.backend.course;

import com.securecourse.backend.toggles.ToggleService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    private final Path rootLocation = Paths.get("uploads");
    private final FileMetadataRepository fileMetadataRepository;
    private final CourseRepository courseRepository;
    private final ToggleService toggleService;

    public FileService(FileMetadataRepository fileMetadataRepository, CourseRepository courseRepository,
            ToggleService toggleService) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.courseRepository = courseRepository;
        this.toggleService = toggleService;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public FileMetadata uploadFile(MultipartFile file, Long courseId) throws IOException {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

        String filename;
        if (toggleService.isFileUploadSecurityEnabled()) {
            // SAFE MODE: Validate and Randomize
            validateFile(file);
            filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        } else {
            // UNSAFE MODE: Trust User Input
            filename = file.getOriginalFilename();
            // Path Traversal Vulnerability: If filename contains "../", it might write
            // outside uploads/
            // But Spring's MultipartFile.getOriginalFilename() usually strips paths.
            // However, we are just demonstrating "Malicious File Upload" (e.g. .php, .html,
            // .sh)
        }

        // Save file
        Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

        // Simple Path Traversal Check for Safe Mode (though UUID prevents it mostly)
        if (toggleService.isFileUploadSecurityEnabled()) {
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // Save Metadata
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalFilename(file.getOriginalFilename());
        metadata.setStoredFilename(filename);
        metadata.setContentType(file.getContentType());
        metadata.setSize(file.getSize());
        metadata.setCourse(course);

        return fileMetadataRepository.save(metadata);
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        // Only allow PNG and JPG when security is ON
        if (contentType == null ||
                (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new RuntimeException("Invalid file type. Only PNG and JPG images are allowed.");
        }

        // Additional filename extension check
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("jpeg")) {
                throw new RuntimeException("Invalid file extension. Only .png and .jpg files are allowed.");
            }
        }
    }

    public Path loadFile(String filename) {
        return rootLocation.resolve(filename);
    }
}
