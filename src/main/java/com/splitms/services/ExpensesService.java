package com.splitms.services;

import com.splitms.interfaces.ExpenseService;
import com.splitms.models.ExpenseModel;
import com.splitms.models.ExpenseSplitModel;
import com.splitms.repositories.ExpenseRepository;
import com.splitms.repositories.ExpenseSplitRepository;
import com.splitms.repositories.JdbcExpenseRepository;
import com.splitms.repositories.JdbcExpenseSplitRepository;
import com.splitms.utils.Normalize;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpensesService implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository splitRepository;

    public ExpensesService() {
        this(new JdbcExpenseRepository(), new JdbcExpenseSplitRepository());
    }

    public ExpensesService(ExpenseRepository expenseRepository, ExpenseSplitRepository splitRepository) {
        this.expenseRepository = expenseRepository;
        this.splitRepository = splitRepository;
    }

    @Override
    public ServiceResult<List<ExpenseModel>> listExpensesForGroup(int groupId) {
        if (groupId <= 0) {
            return ServiceResult.fail("Invalid group id.");
        }

        List<ExpenseModel> expenses = expenseRepository.findByGroup(groupId);
        return ServiceResult.ok("Expenses loaded.", expenses);
    }

    @Override
    public ServiceResult<ExpenseModel> getExpense(int expenseId) {
        if (expenseId <= 0) {
            return ServiceResult.fail("Invalid expense id.");
        }

        return expenseRepository.findById(expenseId)
                .map(e -> ServiceResult.ok("Expense found.", e))
                .orElseGet(() -> ServiceResult.fail("Expense not found."));
    }

    @Override
    public ServiceResult<ExpenseModel> createExpense(int groupId, int payerId, int categoryId,
            BigDecimal amount, LocalDate expenseDate, String description) {
        String normalizedDescription = Normalize.normalizeText(description);

        if (groupId <= 0 || payerId <= 0 || categoryId <= 0) {
            return ServiceResult.fail("Invalid group, payer, or category id.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ServiceResult.fail("Amount must be greater than zero.");
        }
        if (expenseDate == null) {
            return ServiceResult.fail("Expense date is required.");
        }

        String safeDescription = normalizedDescription.isBlank() ? "No description" : normalizedDescription;

        int expenseId = expenseRepository.create(groupId, payerId, categoryId, amount, expenseDate, safeDescription);
        if (expenseId <= 0) {
            return ServiceResult.fail("Could not create expense.");
        }

        return expenseRepository.findById(expenseId)
                .map(e -> ServiceResult.ok("Expense created.", e))
                .orElseGet(() -> ServiceResult.fail("Expense created but could not be loaded."));
    }

    @Override
    public ServiceResult<ExpenseModel> createExpenseWithSplits(int groupId, int payerId,
            int categoryId, BigDecimal amount, LocalDate expenseDate, String description,
            List<ExpenseSplitModel> splits) {

        if (splits == null || splits.isEmpty()) {
            return ServiceResult.fail("At least one split is required.");
        }

        BigDecimal splitsTotal = splits.stream()
                .map(ExpenseSplitModel::shareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (splitsTotal.compareTo(amount) != 0) {
            return ServiceResult.fail(
                    "Split amounts (" + splitsTotal + ") must equal the expense amount (" + amount + ").");
        }

        ServiceResult<ExpenseModel> createResult = createExpense(
                groupId, payerId, categoryId, amount, expenseDate, description);
        if (!createResult.success()) {
            return createResult;
        }

        int expenseId = createResult.data().expenseId();
        for (ExpenseSplitModel split : splits) {
            int splitId = splitRepository.create(expenseId, split.userId(),
                    split.shareAmount(), split.sharePercentage());
            if (splitId <= 0) {
                return ServiceResult.fail("Expense created but a split could not be saved.");
            }
        }

        return expenseRepository.findById(expenseId)
                .map(e -> ServiceResult.ok("Expense with splits created.", e))
                .orElseGet(() -> ServiceResult.fail("Expense created but could not be reloaded."));
    }

    @Override
    public ServiceResult<ExpenseModel> updateExpense(int expenseId, int categoryId,
            BigDecimal amount, LocalDate expenseDate, String description) {
        String normalizedDescription = Normalize.normalizeText(description);

        if (expenseId <= 0 || categoryId <= 0) {
            return ServiceResult.fail("Invalid expense or category id.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ServiceResult.fail("Amount must be greater than zero.");
        }
        if (expenseDate == null) {
            return ServiceResult.fail("Expense date is required.");
        }

        String safeDescription = normalizedDescription.isBlank() ? "No description" : normalizedDescription;

        boolean updated = expenseRepository.update(expenseId, categoryId, amount, expenseDate, safeDescription);
        if (!updated) {
            return ServiceResult.fail("Expense update failed.");
        }

        return expenseRepository.findById(expenseId)
                .map(e -> ServiceResult.ok("Expense updated.", e))
                .orElseGet(() -> ServiceResult.fail("Expense updated but could not be reloaded."));
    }

    @Override
    public ServiceResult<Void> deleteExpense(int expenseId) {
        if (expenseId <= 0) {
            return ServiceResult.fail("Invalid expense id.");
        }

        boolean deleted = expenseRepository.delete(expenseId);
        if (!deleted) {
            return ServiceResult.fail("Expense delete failed.");
        }

        return ServiceResult.ok("Expense deleted.", null);
    }
}
