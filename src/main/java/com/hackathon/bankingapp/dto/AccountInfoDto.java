package com.hackathon.bankingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountInfoDto {
    private String accountNumber;
    private double balance;
}
