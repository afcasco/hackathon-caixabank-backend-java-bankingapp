package com.hackathon.bankingapp.dto;

import lombok.Data;

@Data
public class TransactionRequestDto {
    private String pin;
    private Double amount;
}

