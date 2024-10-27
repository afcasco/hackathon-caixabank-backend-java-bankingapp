package com.hackathon.bankingapp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoInvestRequestDto {
    @NotEmpty(message = "PIN cannot be null or empty")
    @NotNull(message = "PIN cannot be null or empty")
    private String pin;
}