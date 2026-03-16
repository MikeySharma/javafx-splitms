package com.splitms.repositories;

import com.splitms.models.ExpenseSplitModel;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseSplitRepository {
    int create(int expenseId, int userId, BigDecimal shareAmount, float sharePercentage);

    Optional<ExpenseSplitModel> findById(int splitId);

    List<ExpenseSplitModel> findByExpense(int expenseId);

    BigDecimal sumShareAmountByExpense(int expenseId);

    boolean deleteAllForExpense(int expenseId);

    boolean delete(int splitId);
}
