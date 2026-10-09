package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.KycDocumentUploadRequest;
import com.example.Loan_Project.Entity.DocumentVerificationStatus;
import com.example.Loan_Project.Entity.KycDocument;
import com.example.Loan_Project.Exception.FileStorageException;
import com.example.Loan_Project.Repository.KycDocumentRepository;
import com.example.Loan_Project.Service.KycDocumentService;
import com.example.Loan_Project.Util.FileStorageUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class KycDocumentServiceImpl implements KycDocumentService {

    private final KycDocumentRepository kycDocumentRepository;

    public KycDocumentServiceImpl(KycDocumentRepository kycDocumentRepository) {
        this.kycDocumentRepository = kycDocumentRepository;
    }

    @Override
    public KycDocument uploadDocument(
            KycDocumentUploadRequest request) {

        try {

            String filePath =
                    FileStorageUtil.saveFile(request.getFile());

            KycDocument kycDocument = new KycDocument();

            kycDocument.setCustomerId(request.getCustomerId());

            kycDocument.setDocumentType(
                    request.getDocumentType()
            );

            kycDocument.setFilePath(filePath);

            kycDocument.setVerificationStatus(
                    DocumentVerificationStatus.PENDING
            );

            kycDocument.setRemark(null);

            return kycDocumentRepository.save(kycDocument);

        } catch (IOException e) {
            throw new FileStorageException("Failed to store document");
        }
    }

    @Override
    public List<KycDocument> getDocumentsByCustomerId(
            Long customerId) {

        return kycDocumentRepository
                .findByCustomerId(customerId);
    }
}
