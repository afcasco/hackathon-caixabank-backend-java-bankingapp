package com.hackathon.bankingapp.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String accountNumber;
    private String hashedPassword;
}
