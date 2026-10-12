package com.example.Loan_Project.Controller;


import com.example.Loan_Project.DTO.TwoFactorResponse;
import com.example.Loan_Project.ServiceImpl.TwoFactorAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/2fa")
public class TwoFactorController {

    private final TwoFactorAuthService twoFactorAuthService;

    public TwoFactorController(
            TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }

    @PostMapping("/setup")
    public ResponseEntity<TwoFactorResponse> setupTwoFactor(
            @RequestParam String email) {

        TwoFactorResponse response =
                twoFactorAuthService.setupCustomerTwoFactor(email);

        return ResponseEntity.ok(response);
    }
}
