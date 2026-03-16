package com.splitms.repositories;

import com.splitms.models.TransactionModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    int create(int groupId, int fromUserId, int toUserId, BigDecimal amount,
            LocalDate transactionDate);

    Optional<TransactionModel> findById(int transactionId);

    List<TransactionModel> findByGroup(int groupId);

    boolean settle(int transactionId);

    boolean delete(int transactionId);
}
