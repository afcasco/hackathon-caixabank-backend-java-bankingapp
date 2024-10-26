package com.hackathon.bankingapp.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UpdatePinRequestDto {
    @NotBlank
    @Size(min = 4, max = 4)
    private String oldPin;

    @NotBlank
    private String password;

    @NotBlank
    @Size(min = 4, max = 4)
    private String newPin;
}
