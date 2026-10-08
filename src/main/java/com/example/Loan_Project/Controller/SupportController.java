package com.example.Loan_Project.Controller;

import com.example.Loan_Project.DTO.CreateSupportTicketRequest;
import com.example.Loan_Project.DTO.ResolveSupportTicketRequest;
import com.example.Loan_Project.DTO.SupportTicketResponse;

import com.example.Loan_Project.Respsonse.ApiResponse;
import com.example.Loan_Project.Service.SupportService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/customer/{customerId}/ticket")
    public ResponseEntity<ApiResponse<SupportTicketResponse>>
    createTicket(
            @PathVariable Integer customerId,
            @Valid @RequestBody CreateSupportTicketRequest request
    ) {

        SupportTicketResponse response =
                supportService.createTicket(
                        customerId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support ticket created successfully",
                        response
                )
        );
    }

    @GetMapping("/customer/{customerId}/tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicketResponse>>>
    getCustomerTickets(

            @PathVariable Integer customerId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        Page<SupportTicketResponse> response =
                supportService.getCustomerTickets(
                        customerId,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer support tickets fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/officer/tickets")
    public ResponseEntity<ApiResponse<Page<SupportTicketResponse>>>
    getOfficerTickets(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.ASC,
                                "createdAt"
                        )
                );

        Page<SupportTicketResponse> response =
                supportService.getOfficerTickets(
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Open support tickets fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/officer/ticket/{ticketNumber}")
    public ResponseEntity<ApiResponse<SupportTicketResponse>>
    getTicket(
            @PathVariable String ticketNumber
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support ticket fetched successfully",
                        supportService.getTicket(ticketNumber)
                )
        );
    }

    @PutMapping("/officer/ticket/{ticketNumber}/resolve")
    public ResponseEntity<ApiResponse<SupportTicketResponse>>
    resolveTicket(

            @PathVariable String ticketNumber,

            @Valid @RequestBody
            ResolveSupportTicketRequest request
    ) {

        SupportTicketResponse response =
                supportService.resolveTicket(
                        ticketNumber,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Support ticket resolved successfully",
                        response
                )
        );
    }
}