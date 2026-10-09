package com.example.Loan_Project.Util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStorageUtil {


    private static final String UPLOAD_DIR = "uploads/kyc/";

    public static String saveFile(MultipartFile file) throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(file.getOriginalFilename());

        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }

    public static byte[] getFile(String filePath) throws IOException {

        Path path = Paths.get(filePath);

        return Files.readAllBytes(path);
    }

}
