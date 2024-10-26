package com.hackathon.bankingapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ResetToken {
    private String token;
    private Instant expiration;
}
