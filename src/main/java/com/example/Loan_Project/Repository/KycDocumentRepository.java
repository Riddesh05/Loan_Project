package com.example.Loan_Project.Repository;

import com.example.Loan_Project.Entity.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KycDocumentRepository extends JpaRepository<KycDocument,Long>
{

    List<KycDocument> findByCustomerId(Long customerId);

}
