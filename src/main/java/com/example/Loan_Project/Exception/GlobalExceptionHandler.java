package com.example.Loan_Project.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handlEmailExist(EmailAlreadyExistsException e){

        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}