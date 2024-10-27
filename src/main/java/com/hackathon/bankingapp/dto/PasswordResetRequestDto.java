package com.hackathon.bankingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequestDto {

    @NotBlank(message = "Identifier is required")
    private String identifier;
    private String resetToken;
    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Size(max = 128, message = "Password must be less than 128 characters long")
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    @Pattern(regexp = ".*[.@$!%*?&].*", message = "Password must contain at least one special character")
    @Pattern(regexp = "^[\\S]+$", message = "Password cannot contain whitespace")
    private String newPassword;
}
