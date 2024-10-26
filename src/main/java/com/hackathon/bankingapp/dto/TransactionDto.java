package com.hackathon.bankingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Double amount;
    private String transactionType;
    private Long transactionDate;
    private String sourceAccountNumber;
    private String targetAccountNumber;
}
