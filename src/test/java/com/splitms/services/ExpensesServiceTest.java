package com.splitms.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.splitms.models.ExpenseModel;
import com.splitms.models.ExpenseSplitModel;
import com.splitms.repositories.ExpenseRepository;
import com.splitms.repositories.ExpenseSplitRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class ExpensesServiceTest {

    private ExpensesService expensesService;
        private int groupId;
        private int payerUserId;
        private int friendUserId;
        private int categoryId;

    @Before
    public void setUp() {
                expensesService = new ExpensesService(new InMemoryExpenseRepository(), new InMemoryExpenseSplitRepository());
                groupId = 10;
                payerUserId = 20;
                friendUserId = 21;
                categoryId = 30;
    }

    @Test
    public void testCreateGetUpdateDeleteExpense() {
        BigDecimal amount = new BigDecimal("60.00");
        LocalDate today = LocalDate.now();

        ServiceResult<ExpenseModel> createResult = expensesService.createExpense(
                groupId, payerUserId, categoryId, amount, today, "Team lunch");
        assertTrue("Expense create should succeed", createResult.success());
        assertNotNull("Created expense should be returned", createResult.data());
        int expenseId = createResult.data().expenseId();
        assertTrue("Expense id should be positive", expenseId > 0);
        assertEquals("Amount should match", 0, amount.compareTo(createResult.data().amount()));

        ServiceResult<ExpenseModel> getResult = expensesService.getExpense(expenseId);
        assertTrue("Expense should be found", getResult.success());
        assertEquals("Description should match", "Team lunch", getResult.data().description());

        BigDecimal newAmount = new BigDecimal("75.00");
        ServiceResult<ExpenseModel> updateResult = expensesService.updateExpense(
                expenseId, categoryId, newAmount, today, "Team dinner");
        assertTrue("Expense update should succeed", updateResult.success());
        assertEquals("Updated amount should match", 0, newAmount.compareTo(updateResult.data().amount()));
        assertEquals("Updated description should match", "Team dinner", updateResult.data().description());

        ServiceResult<Void> deleteResult = expensesService.deleteExpense(expenseId);
        assertTrue("Expense delete should succeed", deleteResult.success());

        ServiceResult<ExpenseModel> afterDelete = expensesService.getExpense(expenseId);
        assertFalse("Expense should not exist after delete", afterDelete.success());
    }

    @Test
    public void testListExpensesForGroup() {
        LocalDate today = LocalDate.now();
        ServiceResult<ExpenseModel> e1 = expensesService.createExpense(
                groupId, payerUserId, categoryId, new BigDecimal("30.00"), today, "Coffee");
        ServiceResult<ExpenseModel> e2 = expensesService.createExpense(
                groupId, payerUserId, categoryId, new BigDecimal("50.00"), today, "Groceries");
        assertTrue("First expense create should succeed", e1.success());
        assertTrue("Second expense create should succeed", e2.success());

        ServiceResult<List<ExpenseModel>> listResult = expensesService.listExpensesForGroup(groupId);
        assertTrue("List should succeed", listResult.success());
        assertTrue("List should contain at least 2 expenses", listResult.data().size() >= 2);

    }

    @Test
    public void testCreateExpenseWithSplitsThatMatch() {
        BigDecimal total = new BigDecimal("100.00");
        LocalDate today = LocalDate.now();

        List<ExpenseSplitModel> splits = List.of(
                new ExpenseSplitModel(0, 0, payerUserId, new BigDecimal("50.00"), 50.0f),
                new ExpenseSplitModel(0, 0, friendUserId, new BigDecimal("50.00"), 50.0f));

        ServiceResult<ExpenseModel> result = expensesService.createExpenseWithSplits(
                groupId, payerUserId, categoryId, total, today, "Split dinner", splits);

        assertTrue("Expense with matching splits should succeed", result.success());
        assertNotNull("Expense data should be returned", result.data());

    }

    @Test
    public void testCreateExpenseWithSplitsThatDoNotMatchFails() {
        BigDecimal total = new BigDecimal("100.00");
        LocalDate today = LocalDate.now();

        List<ExpenseSplitModel> splits = List.of(
                new ExpenseSplitModel(0, 0, payerUserId, new BigDecimal("40.00"), 40.0f),
                new ExpenseSplitModel(0, 0, friendUserId, new BigDecimal("40.00"), 40.0f));

        ServiceResult<ExpenseModel> result = expensesService.createExpenseWithSplits(
                groupId, payerUserId, categoryId, total, today, "Mismatched split", splits);

        assertFalse("Splits that do not sum to expense amount should fail", result.success());
    }

    @Test
    public void testCreateExpenseWithZeroAmountFails() {
        ServiceResult<ExpenseModel> result = expensesService.createExpense(
                groupId, payerUserId, categoryId, BigDecimal.ZERO, LocalDate.now(), "Zero cost");
        assertFalse("Zero amount should fail", result.success());
    }

    @Test
    public void testCreateExpenseWithInvalidGroupFails() {
        ServiceResult<ExpenseModel> result = expensesService.createExpense(
                0, payerUserId, categoryId, new BigDecimal("10.00"), LocalDate.now(), "Bad group");
        assertFalse("Invalid group id should fail", result.success());
    }

        private static final class InMemoryExpenseRepository implements ExpenseRepository {
                private final Map<Integer, ExpenseModel> expenses = new HashMap<>();
                private int sequence = 1;

                @Override
                public int create(int groupId, int payerId, int categoryId, BigDecimal amount,
                                LocalDate expenseDate, String description) {
                        int id = sequence++;
                        expenses.put(id, new ExpenseModel(id, groupId, payerId, categoryId, amount, expenseDate, description));
                        return id;
                }

                @Override
                public Optional<ExpenseModel> findById(int expenseId) {
                        return Optional.ofNullable(expenses.get(expenseId));
                }

                @Override
                public List<ExpenseModel> findByGroup(int groupId) {
                        List<ExpenseModel> result = new ArrayList<>();
                        for (ExpenseModel expense : expenses.values()) {
                                if (expense.groupId() == groupId) {
                                        result.add(expense);
                                }
                        }
                        return result;
                }

                @Override
                public Optional<BigDecimal> findAmountById(int expenseId) {
                        return findById(expenseId).map(ExpenseModel::amount);
                }

                @Override
                public boolean update(int expenseId, int categoryId, BigDecimal amount,
                                LocalDate expenseDate, String description) {
                        ExpenseModel current = expenses.get(expenseId);
                        if (current == null) {
                                return false;
                        }
                        expenses.put(expenseId, new ExpenseModel(
                                        expenseId,
                                        current.groupId(),
                                        current.payerId(),
                                        categoryId,
                                        amount,
                                        expenseDate,
                                        description));
                        return true;
                }

                @Override
                public boolean delete(int expenseId) {
                        return expenses.remove(expenseId) != null;
                }
        }

        private static final class InMemoryExpenseSplitRepository implements ExpenseSplitRepository {
                private final Map<Integer, ExpenseSplitModel> splits = new HashMap<>();
                private int sequence = 1;

                @Override
                public int create(int expenseId, int userId, BigDecimal shareAmount, float sharePercentage) {
                        int id = sequence++;
                        splits.put(id, new ExpenseSplitModel(id, expenseId, userId, shareAmount, sharePercentage));
                        return id;
                }

                @Override
                public Optional<ExpenseSplitModel> findById(int splitId) {
                        return Optional.ofNullable(splits.get(splitId));
                }

                @Override
                public List<ExpenseSplitModel> findByExpense(int expenseId) {
                        List<ExpenseSplitModel> result = new ArrayList<>();
                        for (ExpenseSplitModel split : splits.values()) {
                                if (split.expenseId() == expenseId) {
                                        result.add(split);
                                }
                        }
                        return result;
                }

                @Override
                public BigDecimal sumShareAmountByExpense(int expenseId) {
                        return findByExpense(expenseId).stream()
                                        .map(ExpenseSplitModel::shareAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                }

                @Override
                public boolean deleteAllForExpense(int expenseId) {
                        splits.values().removeIf(split -> split.expenseId() == expenseId);
                        return true;
                }

                @Override
                public boolean delete(int splitId) {
                        return splits.remove(splitId) != null;
                }
        }
}
