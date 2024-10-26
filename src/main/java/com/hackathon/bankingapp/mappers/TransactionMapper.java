package com.hackathon.bankingapp.mappers;

import com.hackathon.bankingapp.dto.TransactionDto;
import com.hackathon.bankingapp.entities.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public TransactionDto toTransactionDto(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionType().name(),
                transaction.getTransactionDate().toEpochMilli(),
                transaction.getSourceAccountNumber().toString(),
                transaction.getTargetAccountNumber() != null ? transaction.getTargetAccountNumber().toString() : "N/A"
        );
    }

    public List<TransactionDto> toDtoList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toTransactionDto)
                .collect(Collectors.toList());
    }
}
