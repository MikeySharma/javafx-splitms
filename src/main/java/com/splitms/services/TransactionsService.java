package com.splitms.services;

import com.splitms.interfaces.TransactionService;
import com.splitms.models.TransactionModel;
import com.splitms.repositories.JdbcTransactionRepository;
import com.splitms.repositories.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionsService implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionsService() {
        this(new JdbcTransactionRepository());
    }

    public TransactionsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public ServiceResult<List<TransactionModel>> listTransactionsForGroup(int groupId) {
        if (groupId <= 0) {
            return ServiceResult.fail("Invalid group id.");
        }

        List<TransactionModel> transactions = transactionRepository.findByGroup(groupId);
        return ServiceResult.ok("Transactions loaded.", transactions);
    }

    @Override
    public ServiceResult<TransactionModel> getTransaction(int transactionId) {
        if (transactionId <= 0) {
            return ServiceResult.fail("Invalid transaction id.");
        }

        return transactionRepository.findById(transactionId)
                .map(t -> ServiceResult.ok("Transaction found.", t))
                .orElseGet(() -> ServiceResult.fail("Transaction not found."));
    }

    @Override
    public ServiceResult<TransactionModel> createTransaction(int groupId, int fromUserId,
            int toUserId, BigDecimal amount, LocalDate transactionDate) {
        if (groupId <= 0 || fromUserId <= 0 || toUserId <= 0) {
            return ServiceResult.fail("Invalid group or user id.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ServiceResult.fail("Amount must be greater than zero.");
        }
        if (transactionDate == null) {
            return ServiceResult.fail("Transaction date is required.");
        }

        int transactionId = transactionRepository.create(groupId, fromUserId, toUserId, amount, transactionDate);
        if (transactionId <= 0) {
            return ServiceResult.fail("Could not create transaction.");
        }

        return transactionRepository.findById(transactionId)
                .map(t -> ServiceResult.ok("Transaction created.", t))
                .orElseGet(() -> ServiceResult.fail("Transaction created but could not be loaded."));
    }

    @Override
    public ServiceResult<TransactionModel> settleTransaction(int transactionId) {
        if (transactionId <= 0) {
            return ServiceResult.fail("Invalid transaction id.");
        }

        boolean settled = transactionRepository.settle(transactionId);
        if (!settled) {
            return ServiceResult.fail("Transaction settle failed.");
        }

        return transactionRepository.findById(transactionId)
                .map(t -> ServiceResult.ok("Transaction settled.", t))
                .orElseGet(() -> ServiceResult.fail("Transaction settled but could not be reloaded."));
    }

    @Override
    public ServiceResult<Void> deleteTransaction(int transactionId) {
        if (transactionId <= 0) {
            return ServiceResult.fail("Invalid transaction id.");
        }

        boolean deleted = transactionRepository.delete(transactionId);
        if (!deleted) {
            return ServiceResult.fail("Transaction delete failed.");
        }

        return ServiceResult.ok("Transaction deleted.", null);
    }
}
