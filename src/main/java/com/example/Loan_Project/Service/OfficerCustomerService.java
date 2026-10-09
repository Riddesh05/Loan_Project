package com.example.Loan_Project.Service;

import com.example.Loan_Project.DTO.OfficerCustomerResponse;
import com.example.Loan_Project.DTO.OfficerKycDocumentResponse;
import com.example.Loan_Project.Entity.Customer;
import com.example.Loan_Project.Entity.KycDocument;

import java.util.List;

public interface OfficerCustomerService {

    List<OfficerCustomerResponse> getAllCustomers();

    List<OfficerKycDocumentResponse> getCustomerDocuments(Long customerId);

    byte[] getDocumentFile(Long documentId);

    void verifyDocument(Long documentId);

    void rejectDocument(Long documentId, String remark);


}
