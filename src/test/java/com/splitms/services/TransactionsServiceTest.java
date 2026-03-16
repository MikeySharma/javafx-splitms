package com.splitms.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.splitms.models.TransactionModel;
import com.splitms.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class TransactionsServiceTest {

    private TransactionsService transactionsService;
    private int fromUserId;
    private int toUserId;
    private int groupId;

    @Before
    public void setUp() {
        transactionsService = new TransactionsService(new InMemoryTransactionRepository());
        groupId = 10;
        fromUserId = 20;
        toUserId = 21;
    }

    @Test
    public void testCreateGetDeleteTransaction() {
        BigDecimal amount = new BigDecimal("25.00");
        LocalDate today = LocalDate.now();

        ServiceResult<TransactionModel> createResult = transactionsService.createTransaction(
                groupId, fromUserId, toUserId, amount, today);
        assertTrue("Transaction create should succeed", createResult.success());
        assertNotNull("Created transaction should be returned", createResult.data());
        int transactionId = createResult.data().transactionId();
        assertTrue("Transaction id should be positive", transactionId > 0);
        assertFalse("New transaction should not be settled", createResult.data().settled());

        ServiceResult<TransactionModel> getResult = transactionsService.getTransaction(transactionId);
        assertTrue("Transaction should be found", getResult.success());

        ServiceResult<Void> deleteResult = transactionsService.deleteTransaction(transactionId);
        assertTrue("Transaction delete should succeed", deleteResult.success());

        ServiceResult<TransactionModel> afterDelete = transactionsService.getTransaction(transactionId);
        assertFalse("Transaction should not exist after delete", afterDelete.success());
    }

    @Test
    public void testSettleTransaction() {
        ServiceResult<TransactionModel> createResult = transactionsService.createTransaction(
                groupId, fromUserId, toUserId, new BigDecimal("15.00"), LocalDate.now());
        assertTrue("Transaction create should succeed", createResult.success());
        int transactionId = createResult.data().transactionId();

        ServiceResult<TransactionModel> settleResult = transactionsService.settleTransaction(transactionId);
        assertTrue("Settle should succeed", settleResult.success());
        assertTrue("Transaction should be marked settled", settleResult.data().settled());

    }

    @Test
    public void testSettleAlreadySettledIsIdempotent() {
        ServiceResult<TransactionModel> createResult = transactionsService.createTransaction(
                groupId, fromUserId, toUserId, new BigDecimal("10.00"), LocalDate.now());
        assertTrue("Transaction create should succeed", createResult.success());
        int transactionId = createResult.data().transactionId();

        assertTrue("First settle should succeed", transactionsService.settleTransaction(transactionId).success());
        assertTrue("Second settle should also succeed", transactionsService.settleTransaction(transactionId).success());
    }

    @Test
    public void testListTransactionsForGroup() {
        LocalDate today = LocalDate.now();
        ServiceResult<TransactionModel> t1 = transactionsService.createTransaction(
                groupId, fromUserId, toUserId, new BigDecimal("5.00"), today);
        ServiceResult<TransactionModel> t2 = transactionsService.createTransaction(
                groupId, fromUserId, toUserId, new BigDecimal("8.00"), today);
        assertTrue("First transaction create should succeed", t1.success());
        assertTrue("Second transaction create should succeed", t2.success());

        ServiceResult<List<TransactionModel>> listResult = transactionsService.listTransactionsForGroup(groupId);
        assertTrue("List should succeed", listResult.success());
        assertTrue("List should contain at least 2 transactions", listResult.data().size() >= 2);

    }

    @Test
    public void testCreateTransactionWithZeroAmountFails() {
        ServiceResult<TransactionModel> result = transactionsService.createTransaction(
                groupId, fromUserId, toUserId, BigDecimal.ZERO, LocalDate.now());
        assertFalse("Zero amount should fail", result.success());
    }

    @Test
    public void testCreateTransactionWithInvalidGroupFails() {
        ServiceResult<TransactionModel> result = transactionsService.createTransaction(
                0, fromUserId, toUserId, new BigDecimal("10.00"), LocalDate.now());
        assertFalse("Invalid group id should fail", result.success());
    }

    @Test
    public void testSettleWithInvalidIdFails() {
        ServiceResult<TransactionModel> result = transactionsService.settleTransaction(0);
        assertFalse("Invalid transaction id should fail", result.success());
    }

    private static final class InMemoryTransactionRepository implements TransactionRepository {
        private final Map<Integer, TransactionModel> transactions = new HashMap<>();
        private int sequence = 1;

        @Override
        public int create(int groupId, int fromUserId, int toUserId, BigDecimal amount,
                LocalDate transactionDate) {
            int id = sequence++;
            transactions.put(id, new TransactionModel(id, groupId, fromUserId, toUserId, amount, transactionDate, false));
            return id;
        }

        @Override
        public Optional<TransactionModel> findById(int transactionId) {
            return Optional.ofNullable(transactions.get(transactionId));
        }

        @Override
        public List<TransactionModel> findByGroup(int groupId) {
            List<TransactionModel> result = new ArrayList<>();
            for (TransactionModel tx : transactions.values()) {
                if (tx.groupId() == groupId) {
                    result.add(tx);
                }
            }
            return result;
        }

        @Override
        public boolean settle(int transactionId) {
            TransactionModel current = transactions.get(transactionId);
            if (current == null) {
                return false;
            }
            transactions.put(transactionId, new TransactionModel(
                    current.transactionId(),
                    current.groupId(),
                    current.fromUserId(),
                    current.toUserId(),
                    current.amount(),
                    current.transactionDate(),
                    true));
            return true;
        }

        @Override
        public boolean delete(int transactionId) {
            return transactions.remove(transactionId) != null;
        }
    }
}
