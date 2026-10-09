package com.example.Loan_Project.DTO;

import com.example.Loan_Project.Entity.DocumentType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class KycDocumentUploadRequest {

    private Long customerId;

    private DocumentType documentType;

    private MultipartFile file;
}
