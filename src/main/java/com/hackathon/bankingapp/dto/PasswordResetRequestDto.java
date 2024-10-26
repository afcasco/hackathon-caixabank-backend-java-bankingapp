package com.hackathon.bankingapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequestDto {

    @NotBlank(message = "Identifier is required")
    private String identifier;
    private String resetToken;
    private String newPassword;
}
