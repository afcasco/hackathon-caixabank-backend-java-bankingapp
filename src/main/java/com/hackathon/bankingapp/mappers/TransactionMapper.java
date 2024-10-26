package com.hackathon.bankingapp.mappers;

import com.hackathon.bankingapp.dto.TransactionDto;
import com.hackathon.bankingapp.entities.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {

    public TransactionDto.Response toDto(Transaction transaction) {
        return new TransactionDto.Response(
                transaction.getId(),
                transaction.getSourceAccountNumber(),
                transaction.getTargetAccountNumber(),
                transaction.getAmount(),
                transaction.getTransactionType().toString(),
                transaction.getAssetSymbol(),
                transaction.getTransactionDate().toEpochMilli()
        );
    }

    public List<TransactionDto.Response> toDtoList(List<Transaction> transactions) {
        return transactions.stream().map(this::toDto).collect(Collectors.toList());
    }
}
