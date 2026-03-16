package com.splitms.interfaces;

import com.splitms.models.TransactionModel;
import com.splitms.services.ServiceResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    ServiceResult<List<TransactionModel>> listTransactionsForGroup(int groupId);

    ServiceResult<TransactionModel> getTransaction(int transactionId);

    ServiceResult<TransactionModel> createTransaction(int groupId, int fromUserId, int toUserId,
            BigDecimal amount, LocalDate transactionDate);

    ServiceResult<TransactionModel> settleTransaction(int transactionId);

    ServiceResult<Void> deleteTransaction(int transactionId);
}
