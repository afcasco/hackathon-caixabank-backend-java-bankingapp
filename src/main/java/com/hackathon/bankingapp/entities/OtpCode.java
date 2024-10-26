package com.hackathon.bankingapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpCode {
    private String code;
    private String identifier;
    private Instant expiration;
}
