package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.MessageDto;
import com.hackathon.bankingapp.dto.TransactionDto;
import com.hackathon.bankingapp.entities.CustomUserDetails;
import com.hackathon.bankingapp.mappers.MessageMapper;
import com.hackathon.bankingapp.mappers.TransactionMapper;
import com.hackathon.bankingapp.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final MessageMapper messageMapper;
    private final TransactionMapper transactionMapper;

    @PostMapping("/deposit")
    public ResponseEntity<MessageDto> deposit(
            @RequestBody TransactionDto.DepositRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(messageMapper.toMessageDto(
                transactionService.deposit(userDetails.getAccountNumber(), request.getPin(), request.getAmount())
        ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<MessageDto> withdraw(
            @RequestBody TransactionDto.WithdrawRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(messageMapper.toMessageDto(
                transactionService.withdraw(userDetails.getAccountNumber(), request.getPin(), request.getAmount())
        ));
    }

    @PostMapping("/fund-transfer")
    public ResponseEntity<MessageDto> fundTransfer(
            @RequestBody TransactionDto.TransferRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(messageMapper.toMessageDto(
                transactionService.transfer(userDetails.getAccountNumber(), request.getPin(), request.getTargetAccountNumber(), request.getAmount())
        ));
    }

    @PostMapping("/buy-asset")
    public ResponseEntity<String> buyAsset(
            @RequestBody TransactionDto.AssetTransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.buyAsset(userDetails.getAccountNumber(), request.getPin(), request.getAssetSymbol(), request.getAmount())
        );
    }

    @PostMapping("/sell-asset")
    public ResponseEntity<String> sellAsset(
            @RequestBody TransactionDto.AssetTransactionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                transactionService.sellAsset(userDetails.getAccountNumber(), request.getPin(), request.getAssetSymbol(), request.getQuantity())
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto.Response>> getTransactionHistory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                transactionMapper.toDtoList(transactionService.getTransactionHistory(userDetails.getAccountNumber()))
        );
    }
}
