package com.splitms.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionModel(
        int transactionId,
        int groupId,
        int fromUserId,
        int toUserId,
        BigDecimal amount,
        LocalDate transactionDate,
        boolean settled) {
}
