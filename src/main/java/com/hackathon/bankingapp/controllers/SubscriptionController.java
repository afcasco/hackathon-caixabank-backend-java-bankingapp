package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.AutoInvestRequestDto;
import com.hackathon.bankingapp.dto.SubscriptionCancelRequestDto;
import com.hackathon.bankingapp.dto.SubscriptionRequestDto;
import com.hackathon.bankingapp.entities.CustomUserDetails;
import com.hackathon.bankingapp.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-actions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> createSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SubscriptionRequestDto request) {
        return ResponseEntity.ok(
                subscriptionService.createSubscription(
                        userDetails.getAccountNumber(),
                        request.getPin(),
                        request.getAmount(),
                        request.getIntervalSeconds()
                )
        );
    }

    @PostMapping("/enable-auto-invest")
    public ResponseEntity<String> enableAutoInvestBot(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AutoInvestRequestDto request) {
        return ResponseEntity.ok(
                subscriptionService.activateAutoInvestBot(
                        userDetails.getAccountNumber(),
                        request.getPin()
                )
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SubscriptionCancelRequestDto request) {
        return ResponseEntity.ok(
                subscriptionService.cancelSubscription(
                        userDetails.getAccountNumber(),
                        request.getPin()
                )
        );
    }
}
