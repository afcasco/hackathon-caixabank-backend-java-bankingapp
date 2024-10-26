package com.hackathon.bankingapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CreatePinRequestDto {
    @NotBlank
    @Size(min = 4, max = 4)
    private String pin;

    @NotBlank
    private String password;
}
