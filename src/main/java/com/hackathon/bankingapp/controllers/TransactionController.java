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
            @AuthenticationPrincipal UUID accountNumber,
            @RequestParam String pin,
            @RequestParam double amount) {
        String message = transactionService.deposit(accountNumber, pin, amount);
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<MessageDto> withdraw(
            @AuthenticationPrincipal UUID accountNumber,
            @RequestParam String pin,
            @RequestParam double amount) {
        String message = transactionService.withdraw(accountNumber, pin, amount);
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @PostMapping("/fund-transfer")
    public ResponseEntity<MessageDto> fundTransfer(
            @AuthenticationPrincipal UUID accountNumber,
            @RequestParam UUID targetAccountNumber,
            @RequestParam String pin,
            @RequestParam double amount) {
        String message = transactionService.transfer(accountNumber, pin, targetAccountNumber, amount);
        return ResponseEntity.ok(messageMapper.toMessageDto(message));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(
            @AuthenticationPrincipal UUID accountNumber) {
        List<TransactionDto> transactionHistory = transactionMapper.toDtoList(transactionService.getTransactionHistory(accountNumber));
        return ResponseEntity.ok(transactionHistory);
    }
}
