package com.splitms.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseModel(
        int expenseId,
        int groupId,
        int payerId,
        int categoryId,
        BigDecimal amount,
        LocalDate expenseDate,
        String description) {
}
