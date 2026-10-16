
package com.example.Loan_Project.Util;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileStorageUtil {

    private static final Path UPLOAD_DIR =
            Paths.get("uploads", "kyc")
                    .toAbsolutePath()
                    .normalize();

    public static String saveFile(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("Uploaded file is empty.");
        }

        Files.createDirectories(UPLOAD_DIR);

        String originalFilename =
                StringUtils.cleanPath(
                        file.getOriginalFilename() == null
                                ? "document"
                                : file.getOriginalFilename()
                );

        if (originalFilename.contains("..")
                || originalFilename.contains("/")
                || originalFilename.contains("\\")) {
            throw new IOException("Invalid filename.");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');

        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID() + extension;

        Path destination =
                UPLOAD_DIR.resolve(storedFilename).normalize();

        if (!destination.startsWith(UPLOAD_DIR)) {
            throw new IOException("Invalid file path.");
        }

        Files.copy(
                file.getInputStream(),
                destination
        );

        return Paths.get("uploads", "kyc", storedFilename)
                .toString()
                .replace("\\", "/");
    }

    public static byte[] getFile(String filePath)
            throws IOException {

        Path path = Paths.get(filePath)
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("Document file not found.");
        }

        return Files.readAllBytes(path);
    }
}
