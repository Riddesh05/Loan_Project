package com.example.Loan_Project.DTO;

import com.example.Loan_Project.Entity.DocumentType;
import com.example.Loan_Project.Entity.DocumentVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OfficerKycDocumentResponse {

    private Long documentId;
    private DocumentType documentType;
    private DocumentVerificationStatus verificationStatus;
    private String remark;
}