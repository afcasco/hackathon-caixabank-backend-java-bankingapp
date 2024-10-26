package com.hackathon.bankingapp.exceptions;

public class PinNotSetException extends RuntimeException {
    public PinNotSetException(String message) {
        super(message);
    }
}