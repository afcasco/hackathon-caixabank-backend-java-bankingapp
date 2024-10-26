package com.hackathon.bankingapp.mappers;

import com.hackathon.bankingapp.dto.TransactionDto;
import com.hackathon.bankingapp.entities.Transaction;
import org.springframework.stereotype.Component;

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
}
