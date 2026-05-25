package com.example.production.service;

import com.example.production.exception.AppException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MockQuestionImageService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Path UPLOAD_DIR = Path.of("uploads", "mock-questions").toAbsolutePath().normalize();

    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest("File ảnh không được để trống");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw AppException.badRequest("Ảnh không được vượt quá 5MB");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw AppException.badRequest("Ảnh chỉ hỗ trợ định dạng jpg, jpeg, png hoặc webp");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw AppException.badRequest("File upload phải là ảnh");
        }

        try {
            Files.createDirectories(UPLOAD_DIR);
            String fileName = UUID.randomUUID() + "." + extension;
            Path target = UPLOAD_DIR.resolve(fileName).normalize();
            if (!target.startsWith(UPLOAD_DIR)) {
                throw AppException.badRequest("Tên file không hợp lệ");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return Map.of("imageUrl", "/uploads/mock-questions/" + fileName);
        } catch (IOException ex) {
            throw AppException.badRequest("Không lưu được ảnh, vui lòng thử lại");
        }
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
    }
}
