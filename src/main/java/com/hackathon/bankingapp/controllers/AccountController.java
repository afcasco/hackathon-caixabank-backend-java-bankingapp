package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.CreatePinRequestDto;
import com.hackathon.bankingapp.dto.PinResponseDto;
import com.hackathon.bankingapp.dto.UpdatePinRequestDto;
import com.hackathon.bankingapp.entities.CustomUserDetails;
import com.hackathon.bankingapp.mappers.PinMapper;
import com.hackathon.bankingapp.services.AccountService;
import com.hackathon.bankingapp.services.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final PinService pinService;
    private final PinMapper pinMapper;
    private final AccountService accountService;

    @PostMapping("/pin/create")
    public ResponseEntity<PinResponseDto> createPin(
            @RequestBody CreatePinRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return processPinAction(() -> pinService.createPin(
                userDetails.getAccountNumber(),
                requestDto.getPin(),
                requestDto.getPassword())
        );
    }

    @PostMapping("/pin/update")
    public ResponseEntity<PinResponseDto> updatePin(
            @RequestBody UpdatePinRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return processPinAction(() -> pinService.updatePin(
                userDetails.getAccountNumber(),
                requestDto.getOldPin(),
                requestDto.getPassword(),
                requestDto.getNewPin())
        );
    }

    @GetMapping("/net-worth")
    public ResponseEntity<Double> getNetWorth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        double netWorth = accountService.getNetWorth(userDetails.getAccountNumber().toString());
        return ResponseEntity.ok(netWorth);
    }

    @GetMapping("/assets")
    public ResponseEntity<Map<String, Double>> getAllAssets(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Double> assets = accountService.getAllAssets(userDetails.getAccountNumber().toString());
        return ResponseEntity.ok(assets);
    }

    private ResponseEntity<PinResponseDto> processPinAction(PinAction action) {
        String response = action.execute();
        return ResponseEntity.ok(pinMapper.toPinResponseDto(response));
    }

    @FunctionalInterface
    private interface PinAction {
        String execute();
    }
}
