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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final MessageMapper messageMapper;
    private final TransactionMapper transactionMapper;

    @PostMapping("/deposit")
    public ResponseEntity<MessageDto> deposit(
            @RequestBody Double amount,
            @RequestHeader("pin") String pin,
            @AuthenticationPrincipal UUID accountNumber) {
        String responseMessage = transactionService.deposit(accountNumber, pin, amount);
        return ResponseEntity.ok(messageMapper.toMessageDto(responseMessage));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<MessageDto> withdraw(
            @RequestBody Double amount,
            @RequestHeader("pin") String pin,
            @AuthenticationPrincipal UUID accountNumber) {
        String responseMessage = transactionService.withdraw(accountNumber, pin, amount);
        return ResponseEntity.ok(messageMapper.toMessageDto(responseMessage));
    }

    @PostMapping("/fund-transfer")
    public ResponseEntity<MessageDto> transfer(
            @RequestBody Double amount,
            @RequestHeader("pin") String pin,
            @RequestParam UUID targetAccountNumber,
            @AuthenticationPrincipal UUID accountNumber) {
        String responseMessage = transactionService.transfer(accountNumber, pin, targetAccountNumber, amount);
        return ResponseEntity.ok(messageMapper.toMessageDto(responseMessage));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDto>> getTransactionHistory(@AuthenticationPrincipal UUID accountNumber) {
        List<TransactionDto> transactions = transactionService.getTransactionHistory(accountNumber).stream()
                .map(transactionMapper::toTransactionDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }
}
