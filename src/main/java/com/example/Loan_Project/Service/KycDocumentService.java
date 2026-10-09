package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.KycDocumentUploadRequest;
import com.example.Loan_Project.Entity.KycDocument;

import java.util.List;

public interface KycDocumentService {

    KycDocument uploadDocument(KycDocumentUploadRequest request);

    List<KycDocument> getDocumentsByCustomerId(Long customerId);
}
