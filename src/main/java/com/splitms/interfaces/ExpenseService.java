package com.splitms.interfaces;

import com.splitms.models.ExpenseModel;
import com.splitms.models.ExpenseSplitModel;
import com.splitms.services.ServiceResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ServiceResult<List<ExpenseModel>> listExpensesForGroup(int groupId);

    ServiceResult<ExpenseModel> getExpense(int expenseId);

    ServiceResult<ExpenseModel> createExpense(int groupId, int payerId, int categoryId,
            BigDecimal amount, LocalDate expenseDate, String title, String description);

    /**
     * Creates an expense together with its splits. Rejects if the splits'
     * share_amount values do not sum exactly to {@code amount}.
     */
    ServiceResult<ExpenseModel> createExpenseWithSplits(int groupId, int payerId, int categoryId,
            BigDecimal amount, LocalDate expenseDate, String title, String description,
            List<ExpenseSplitModel> splits);

    ServiceResult<ExpenseModel> updateExpense(int expenseId, int categoryId,
            BigDecimal amount, LocalDate expenseDate, String title, String description);

    ServiceResult<Void> deleteExpense(int expenseId);
}
