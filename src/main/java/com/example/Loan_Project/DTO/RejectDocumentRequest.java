package com.example.Loan_Project.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectDocumentRequest {

    @NotBlank
    private String remark;
}
