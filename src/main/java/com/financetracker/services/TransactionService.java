package com.financetracker.services;

import com.financetracker.model.Budget;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Created by blagoy
 */
public interface TransactionService {

    List<Transaction> getAllTransactionsByAccountId(long accountId);

    List<Transaction> getAllTransactionsByCategoryId(long categoryId);

    Transaction getTransactionByTransactionId(long transactionId);

    void insertTransaction(Transaction transaction);

    void updateTransaction(Transaction transaction);

    void postTransaction(User user, String account, String category, String type, LocalDateTime date, String amount,
                         Transaction transaction, long transactionId);

    void deleteTransaction(User user, long transactionId);

    boolean existsTransaction(Budget budget);

    boolean isBetweenTwoDates(LocalDateTime date, LocalDateTime from, LocalDateTime to);

    Set<Transaction> getAllTransactionsForBudget(Budget budget);

}
