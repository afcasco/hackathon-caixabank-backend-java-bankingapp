package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.*;
import com.hackathon.bankingapp.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final OtpService otpService;

    @PostMapping("/send-otp")
    public ResponseEntity<OtpResponseDto> sendOtp(@RequestBody PasswordResetRequestDto requestDto) {
        otpService.sendOtp(requestDto.getIdentifier());
        String message = String.format("OTP sent successfully to: %s", requestDto.getIdentifier());
        return ResponseEntity.ok(new OtpResponseDto(message));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponseDto> verifyOtp(@RequestBody VerifyOtpRequestDto requestDto) {
        String resetToken = otpService.verifyOtp(requestDto.getIdentifier(), requestDto.getOtp());
        return ResponseEntity.ok(new VerifyOtpResponseDto(resetToken));
    }

    @PostMapping
    public ResponseEntity<PasswordResetResponseDto> resetPassword(@RequestBody PasswordResetRequestDto requestDto) {
        otpService.resetPassword(requestDto.getIdentifier(), requestDto.getResetToken(), requestDto.getNewPassword());
        return ResponseEntity.ok(new PasswordResetResponseDto("Password reset successfully"));
    }
}
