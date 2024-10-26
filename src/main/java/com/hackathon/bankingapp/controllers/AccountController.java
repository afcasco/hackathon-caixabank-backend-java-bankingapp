package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.CreatePinRequestDto;
import com.hackathon.bankingapp.dto.PinResponseDto;
import com.hackathon.bankingapp.dto.UpdatePinRequestDto;
import com.hackathon.bankingapp.mappers.PinMapper;
import com.hackathon.bankingapp.services.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final PinService pinService;
    private final PinMapper pinMapper;

    @PostMapping("/pin/create")
    public ResponseEntity<PinResponseDto> createPin(
            @RequestBody CreatePinRequestDto requestDto,
            @AuthenticationPrincipal UUID accountNumber) {
        String response = pinService.createPin(accountNumber, requestDto.getPin(), requestDto.getPassword());
        return ResponseEntity.ok(pinMapper.toPinResponseDto(response));
    }

    @PostMapping("/pin/update")
    public ResponseEntity<PinResponseDto> updatePin(
            @RequestBody UpdatePinRequestDto requestDto,
            @AuthenticationPrincipal UUID accountNumber) {
        String response = pinService.updatePin(accountNumber, requestDto.getOldPin(), requestDto.getPassword(), requestDto.getNewPin());
        return ResponseEntity.ok(pinMapper.toPinResponseDto(response));
    }
}
