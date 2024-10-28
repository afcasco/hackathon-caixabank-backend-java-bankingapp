package com.hackathon.bankingapp.exceptions;

public class InsufficientBalanceException400 extends RuntimeException {
    public InsufficientBalanceException400(String message) {
        super(message);
    }
}