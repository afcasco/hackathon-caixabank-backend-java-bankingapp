package com.hackathon.bankingapp.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TransferRequestDto {
    private String pin;
    private Double amount;
    private UUID targetAccountNumber;
}