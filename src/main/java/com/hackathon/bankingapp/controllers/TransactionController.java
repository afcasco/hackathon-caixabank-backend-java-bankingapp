package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.MessageDto;
import com.hackathon.bankingapp.dto.TransactionDto;
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
            @AuthenticationPrincipal UUID accountNumber) {
        String message = transactionService.deposit(accountNumber, request.getPin(), request.getAmount());
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<MessageDto> withdraw(
            @RequestBody TransactionDto.WithdrawRequest request,
            @AuthenticationPrincipal UUID accountNumber) {
        String message = transactionService.withdraw(accountNumber, request.getPin(), request.getAmount());
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @PostMapping("/fund-transfer")
    public ResponseEntity<MessageDto> fundTransfer(
            @RequestBody TransactionDto.TransferRequest request,
            @AuthenticationPrincipal UUID accountNumber) {
        String message = transactionService.transfer(accountNumber, request.getPin(), request.getTargetAccountNumber(), request.getAmount());
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @PostMapping("/buy-asset")
    public ResponseEntity<MessageDto> buyAsset(
            @RequestBody TransactionDto.AssetTransactionRequest request,
            @AuthenticationPrincipal UUID accountNumber) {
        String message = transactionService.buyAsset(accountNumber, request.getPin(), request.getAssetSymbol(), request.getAmount());
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @PostMapping("/sell-asset")
    public ResponseEntity<MessageDto> sellAsset(
            @RequestBody TransactionDto.AssetTransactionRequest request,
            @AuthenticationPrincipal UUID accountNumber) {
        String message = transactionService.sellAsset(accountNumber, request.getPin(), request.getAssetSymbol(), request.getQuantity());
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto.Response>> getTransactionHistory(@AuthenticationPrincipal UUID accountNumber) {
        List<TransactionDto.Response> transactionHistory = transactionMapper.toDtoList(transactionService.getTransactionHistory(accountNumber));
        return ResponseEntity.ok(transactionHistory);
    }
}
