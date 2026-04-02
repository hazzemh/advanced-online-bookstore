package com.example.bookstore.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private final Path uploadDir;

    public FileService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir);
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Created upload directory: {}", uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String uploadImage(MultipartFile file) {
        validateFile(file);

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        Path filePath = uploadDir.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded successfully: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("Failed to upload file: {}", fileName, e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public void deleteImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        try {
            Path filePath = uploadDir.resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileName);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileName, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: JPEG, PNG, GIF, WebP");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name");
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "jpg"; // default extension
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public boolean isImageFile(String fileName) {
        if (fileName == null) return false;
        String extension = getFileExtension(fileName);
        return Arrays.asList("jpg", "jpeg", "png", "gif", "webp").contains(extension);
    }

    public Resource getImageResource(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        Path filePath = uploadDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + fileName);
        }

        return new FileSystemResource(filePath);
    }
}
