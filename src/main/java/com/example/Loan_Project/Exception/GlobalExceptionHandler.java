package com.example.Loan_Project.Exception;

import com.example.Loan_Project.Respsonse.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==========================================
    // RESOURCE NOT FOUND
    // ==========================================

    @ExceptionHandler(
            ResourceNotFoundException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleResourceNotFound(
            ResourceNotFoundException e
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponse.error(
                                e.getMessage()
                        )
                );
    }

    // ==========================================
    // BAD REQUEST
    // ==========================================

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleIllegalArgument(
            IllegalArgumentException e
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.error(
                                e.getMessage()
                        )
                );
    }

    // ==========================================
    // VALIDATION ERROR
    // ==========================================

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleValidationException(
            MethodArgumentNotValidException e
    ) {

        String message =
                e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error ->
                                error.getDefaultMessage()
                        )
                        .orElse(
                                "Invalid request"
                        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiResponse.error(
                                message
                        )
                );
    }

    // ==========================================
    // GENERAL ERROR
    // ==========================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleGeneralException(
            Exception e
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(
                        ApiResponse.error(
                                "An unexpected error occurred"
                        )
                );
    }
}