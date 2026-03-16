package com.splitms.models;

import java.math.BigDecimal;

public record ExpenseSplitModel(
        int splitId,
        int expenseId,
        int userId,
        BigDecimal shareAmount,
        float sharePercentage) {
}
