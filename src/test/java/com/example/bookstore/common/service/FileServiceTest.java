package com.example.bookstore.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceTest {

    @Autowired
    private FileService fileService;

    private static final String UPLOAD_DIR = "uploads";

    @BeforeEach
    void setUp() {
        // Clean up test files
        cleanupTestFiles();
    }

    @Test
    void testUploadImageSuccess() {
        // Arrange
        byte[] imageContent = "test image content".getBytes();
        MultipartFile multipartFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                "image/jpeg",
                imageContent
        );

        // Act
        String fileName = fileService.uploadImage(multipartFile);

        // Assert
        assertNotNull(fileName);
        assertTrue(fileName.endsWith(".jpg"));
        assertTrue(Files.exists(Paths.get(UPLOAD_DIR, fileName)));

        // Cleanup
        fileService.deleteImage(fileName);
    }

    @Test
    void testUploadImageWithEmptyFile() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadImage(emptyFile);
        });
    }

    @Test
    void testUploadImageExceedsMaxSize() {
        // Arrange
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MultipartFile largeFile = new MockMultipartFile(
                "image",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadImage(largeFile);
        });
    }

    @Test
    void testUploadImageWithInvalidContentType() {
        // Arrange
        byte[] pdfContent = "%PDF-1.4".getBytes();
        MultipartFile pdfFile = new MockMultipartFile(
                "image",
                "document.pdf",
                "application/pdf",
                pdfContent
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadImage(pdfFile);
        });
    }

    @Test
    void testUploadImageWithValidContentTypes() {
        String[] validContentTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};

        for (String contentType : validContentTypes) {
            // Arrange
            byte[] imageContent = "test image".getBytes();
            String extension = contentType.split("/")[1];
            MultipartFile multipartFile = new MockMultipartFile(
                    "image",
                    "test." + extension,
                    contentType,
                    imageContent
            );

            // Act
            String fileName = fileService.uploadImage(multipartFile);

            // Assert
            assertNotNull(fileName);
            assertTrue(Files.exists(Paths.get(UPLOAD_DIR, fileName)));

            // Cleanup
            fileService.deleteImage(fileName);
        }
    }

    @Test
    void testDeleteImageSuccess() throws IOException {
        // Arrange
        byte[] imageContent = "test image".getBytes();
        MultipartFile multipartFile = new MockMultipartFile(
                "image",
                "delete-test.jpg",
                "image/jpeg",
                imageContent
        );
        String fileName = fileService.uploadImage(multipartFile);
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        assertTrue(Files.exists(filePath));

        // Act
        fileService.deleteImage(fileName);

        // Assert
        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteNonExistentImage() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> fileService.deleteImage("non-existent-file.jpg"));
    }

    @Test
    void testDeleteNullImageName() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> fileService.deleteImage(null));
    }

    @Test
    void testIsImageFile() {
        assertTrue(fileService.isImageFile("photo.jpg"));
        assertTrue(fileService.isImageFile("picture.jpeg"));
        assertTrue(fileService.isImageFile("image.png"));
        assertTrue(fileService.isImageFile("photo.gif"));
        assertTrue(fileService.isImageFile("image.webp"));

        assertFalse(fileService.isImageFile("document.pdf"));
        assertFalse(fileService.isImageFile("video.mp4"));
        assertFalse(fileService.isImageFile(null));
    }

    @Test
    void testUploadImageWithPathTraversalAttempt() {
        // Arrange
        byte[] imageContent = "test image".getBytes();
        MultipartFile maliciousFile = new MockMultipartFile(
                "image",
                "../../../etc/passwd",
                "image/jpeg",
                imageContent
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fileService.uploadImage(maliciousFile);
        });
    }

    private void cleanupTestFiles() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (Files.exists(uploadPath)) {
                Files.list(uploadPath)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // Ignore cleanup errors
                            }
                        });
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }
}

