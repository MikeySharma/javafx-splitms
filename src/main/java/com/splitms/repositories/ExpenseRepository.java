package com.splitms.repositories;

import com.splitms.models.ExpenseModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository {
    int create(int groupId, int payerId, int categoryId, BigDecimal amount,
            LocalDate expenseDate, String description);

    Optional<ExpenseModel> findById(int expenseId);

    List<ExpenseModel> findByGroup(int groupId);

    Optional<BigDecimal> findAmountById(int expenseId);

    boolean update(int expenseId, int categoryId, BigDecimal amount,
            LocalDate expenseDate, String description);

    boolean delete(int expenseId);
}
