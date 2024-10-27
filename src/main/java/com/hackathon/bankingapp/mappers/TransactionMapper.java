package com.hackathon.bankingapp.mappers;

import com.hackathon.bankingapp.dto.TransactionDto;
import com.hackathon.bankingapp.entities.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionMapper {

    public List<TransactionDto.Response> toDtoList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionDto.Response toResponse(Transaction transaction) {
        return new TransactionDto.Response(
                transaction.getId(),
                transaction.getSourceAccountNumber(),
                transaction.getTargetAccountNumber(),
                transaction.getAmount(),
                transaction.getTransactionType().name(),
                transaction.getTransactionDate()
        );
    }
}
