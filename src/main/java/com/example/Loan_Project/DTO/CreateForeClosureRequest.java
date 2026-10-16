
package com.example.Loan_Project.DTO;

import com.example.Loan_Project.Entity.ForeClosureType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateForeClosureRequest {

    @NotNull(message = "Loan account ID is required")
    private Integer loanAccountId;

    @NotNull(message = "Foreclosure type is required")
    @JsonProperty("foreclosureType")
    private ForeClosureType foreClosureType;

    @Min(value = 1, message = "Select at least one month")
    private Integer numberOfMonths;

    private LocalDateTime expectedClosureDate;

    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;
}
