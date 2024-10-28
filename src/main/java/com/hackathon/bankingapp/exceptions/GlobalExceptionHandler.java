package com.hackathon.bankingapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    public static final String ERROR = "error";

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<String> handleInvalidOtpException(InvalidOtpException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                "Invalid OTP");
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResetTokenException(InvalidResetTokenException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(ERROR, "Invalid reset token")
        );
    }

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<String> handleInvalidPinException(InvalidPinException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(ERROR, "Unable to process request")
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        Optional<String> emailError = fieldErrors.stream()
                .filter(error -> "email".equals(error.getField()))
                .map(FieldError::getDefaultMessage)
                .findFirst();

        if (emailError.isPresent()) {
            return emailError.get();
        }

        List<String> errorMessages = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        boolean hasWhitespaceError = errorMessages.contains("Password cannot contain whitespace");

        if (hasWhitespaceError) {
            errorMessages = new ArrayList<>();
            errorMessages.add("Password cannot contain whitespace");
        } else {
            boolean missingDigit = errorMessages.contains("Password must contain at least one digit");
            boolean missingSpecialChar = errorMessages.contains("Password must contain at least one special character");

            if (missingDigit && missingSpecialChar) {
                errorMessages.remove("Password must contain at least one digit");
                errorMessages.remove("Password must contain at least one special character");
                errorMessages.add("Password must contain at least one digit and one special character");
            }
        }

        return errorMessages.stream()
                .distinct()
                .collect(Collectors.joining(" and "));
    }



    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InsufficientBalanceException400.class)
    public ResponseEntity<String> handleInsufficientBalanceException(InsufficientBalanceException400 ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


}
