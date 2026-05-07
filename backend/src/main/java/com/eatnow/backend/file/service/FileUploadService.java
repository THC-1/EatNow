package com.eatnow.backend.file.service;

import com.eatnow.backend.common.exception.BusinessException;
import com.eatnow.backend.file.vo.UploadImageVo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Path UPLOAD_ROOT = Path.of("uploads");
    private static final Set<String> ALLOWED_TYPES = Set.of("avatar", "dish", "review", "post", "merchant-logo");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    public UploadImageVo uploadImage(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "file must not be empty");
        }
        validateImageContentType(file.getContentType());

        String normalizedType = normalizeType(type);
        String extension = extractExtension(file.getOriginalFilename());
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String filename = UUID.randomUUID() + "." + extension;
        Path targetDirectory = UPLOAD_ROOT.resolve(normalizedType).resolve(datePath).normalize();
        Path targetFile = targetDirectory.resolve(filename).normalize();

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetFile.toAbsolutePath());
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }

        return new UploadImageVo("/uploads/" + normalizedType + "/" + datePath + "/" + filename);
    }

    private String normalizeType(String type) {
        if (!StringUtils.hasText(type)) {
            return "dish";
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "type is invalid");
        }
        return normalized;
    }

    private void validateImageContentType(String contentType) {
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "file must be an image");
        }
    }

    private String extractExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "image filename is invalid");
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "image filename is invalid");
        }
        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "image type is invalid");
        }
        return extension;
    }
}
