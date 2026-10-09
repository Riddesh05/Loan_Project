package com.example.Loan_Project.ServiceImpl;

import com.example.Loan_Project.DTO.OfficerCustomerResponse;
import com.example.Loan_Project.DTO.OfficerKycDocumentResponse;
import com.example.Loan_Project.Entity.DocumentVerificationStatus;
import com.example.Loan_Project.Entity.KycDocument;
import com.example.Loan_Project.Exception.DocumentNotFoundException;
import com.example.Loan_Project.Exception.FileStorageException;
import com.example.Loan_Project.Repository.CustomerRepository;
import com.example.Loan_Project.Repository.KycDocumentRepository;
import com.example.Loan_Project.Service.OfficerCustomerService;
import com.example.Loan_Project.Util.FileStorageUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class OfficerCustomerServiceImpl
        implements OfficerCustomerService {

    private final CustomerRepository customerRepository;
    private final KycDocumentRepository kycDocumentRepository;

    public OfficerCustomerServiceImpl(
            CustomerRepository customerRepository, KycDocumentRepository kycDocumentRepository) {

        this.customerRepository = customerRepository;
        this.kycDocumentRepository = kycDocumentRepository;
    }

    @Override
    public List<OfficerCustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(customer -> new OfficerCustomerResponse(
                        customer.getCustomerId(),
                        customer.getFirstName() + " " + customer.getLastName(),
                        customer.getEmail(),
                        customer.getMobileNo()
                ))
                .toList();
    }

    @Override
    public List<OfficerKycDocumentResponse> getCustomerDocuments(Long customerId) {

        return kycDocumentRepository.findByCustomerId(customerId)
                .stream()
                .map(document -> new OfficerKycDocumentResponse(
                        document.getDocumentId(),
                        document.getDocumentType(),
                        document.getVerificationStatus(),
                        document.getRemark()
                ))
                .toList();
    }

    @Override
    public byte[] getDocumentFile(Long documentId) {

        KycDocument document = kycDocumentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new DocumentNotFoundException("Document not found"));

        try {
            return FileStorageUtil.getFile(document.getFilePath());
        } catch (IOException e) {
            throw new FileStorageException(
                    "Unable to read document"
            );
        }
    }

    @Override
    public void verifyDocument(Long documentId) {

        KycDocument document = kycDocumentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new DocumentNotFoundException("Document not found"));

        document.setVerificationStatus(
                DocumentVerificationStatus.VERIFIED
        );

        document.setRemark(null);

        kycDocumentRepository.save(document);
    }

    @Override
    public void rejectDocument(Long documentId, String remark) {

        KycDocument document = kycDocumentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new DocumentNotFoundException("Document not found"));

        document.setVerificationStatus(
                DocumentVerificationStatus.REJECTED
        );

        document.setRemark(remark);

        kycDocumentRepository.save(document);
    }
}