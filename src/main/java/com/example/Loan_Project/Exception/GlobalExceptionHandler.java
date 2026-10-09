package com.example.Loan_Project.Exception;


import com.example.Loan_Project.Response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleEmailExist(
            EmailAlreadyExistsException e) {

        ApiResponse<String> response =
                new ApiResponse<>(
                        false,
                        e.getMessage(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidCredentials(
            InvalidCredentialsException exception) {

        ApiResponse<String> response =
                new ApiResponse<>(
                        false,
                        exception.getMessage(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }


    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<String>> handleEmailNotVerified(
            EmailNotVerifiedException exception)
    {
        ApiResponse<String> response =
                new ApiResponse<>(
                        false,
                        exception.getMessage(),
                        null
                );

        return  ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);

    }


    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<String>> handleFileStorageException(
            FileStorageException exception) {

        ApiResponse<String> response =
                new ApiResponse<>(
                        false,
                        exception.getMessage(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleDocumentNotFound(
            DocumentNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiResponse<>(
                                false,
                                exception.getMessage(),
                                null
                        )
                );
    }
}