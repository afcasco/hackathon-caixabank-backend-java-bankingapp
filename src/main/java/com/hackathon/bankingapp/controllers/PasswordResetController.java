package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.PasswordResetRequestDto;
import com.hackathon.bankingapp.dto.VerifyOtpRequestDto;
import com.hackathon.bankingapp.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody PasswordResetRequestDto requestDto) {
        otpService.sendOtp(requestDto.getIdentifier());
        return ResponseEntity.ok(
                Map.of("message", String.format("OTP sent successfully to: %s", requestDto.getIdentifier()))
        );
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody VerifyOtpRequestDto requestDto) {
        String resetToken = otpService.verifyOtp(requestDto.getIdentifier(), requestDto.getOtp());
        return ResponseEntity.ok(Map.of("passwordResetToken", resetToken));
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequestDto requestDto) {
        otpService.resetPassword(requestDto.getIdentifier(), requestDto.getResetToken(), requestDto.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
