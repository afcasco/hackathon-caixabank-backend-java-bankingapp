package com.hackathon.bankingapp.exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Bad credentials");
    }
}