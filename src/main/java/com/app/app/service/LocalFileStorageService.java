package com.app.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Conditional(LocalFileStorageService.LocalEnabled.class)
public class LocalFileStorageService implements FileStorageService {

    @Value("${storage.local.base-dir:uploads}")
    private String baseDir;

    @Value("${storage.local.public-url-base:/uploads}")
    private String publicUrlBase;

    static class LocalEnabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String provider = context.getEnvironment().getProperty("storage.provider", "local");
            return "local".equalsIgnoreCase(provider);
        }
    }

    @Override
    public String upload(MultipartFile file, String folder) throws IOException {
        Path dir = Path.of(baseDir, folder);
        Files.createDirectories(dir);

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path target = dir.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return publicUrlBase + "/" + folder + "/" + filename;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}