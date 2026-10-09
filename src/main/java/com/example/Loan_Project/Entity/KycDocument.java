package com.example.Loan_Project.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "KycDocuments")
@Data
public class KycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DocumentId")
    private Long documentId;

    @Column(name = "CustomerId", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "DocumentType", nullable = false)
    private DocumentType documentType;

    @Column(name = "FilePath")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "VerificationStatus", nullable = false)
    private DocumentVerificationStatus verificationStatus;

    @Column(name = "Remark")
    private String remark;
}
