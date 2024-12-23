package com.hackathon.bankingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Double amount;
    private String transactionType;
    private Long transactionDate;
    private String sourceAccountNumber;
    private String targetAccountNumber;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepositRequest {
        private String pin;
        private Double amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WithdrawRequest {
        private String pin;
        private Double amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequest {
        private String pin;
        private UUID targetAccountNumber;
        private Double amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetTransactionRequest {
        private String pin;
        private String assetSymbol;
        private Double amount;
        private Double quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Double amount;
        private String transactionType;
        private Long transactionDate;
        private String sourceAccountNumber;
        private String targetAccountNumber;


        public Response(Long id, UUID sourceAccount, UUID targetAccount, Double amount, String transactionType, Instant transactionDate) {
            this.id = id;
            this.amount = amount;
            this.transactionType = transactionType;
            this.transactionDate = transactionDate.toEpochMilli();
            this.sourceAccountNumber = sourceAccount.toString();
            this.targetAccountNumber = (targetAccount != null) ? targetAccount.toString() : "N/A";
        }
    }
}
