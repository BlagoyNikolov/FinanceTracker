package com.financetracker.services;

import com.financetracker.model.Account;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.PaymentType;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionServiceImpl implements TransactionService {

    public static final String INCOME = "INCOME";
    public static final String EXPENSE = "EXPENSE";

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserService userService;

    public List<Transaction> getAllTransactionsByAccountId(long accountId) {
        return transactionRepository.findByAccountAccountId(accountId);
    }

    public List<Transaction> getAllTransactionsByCategoryId(long categoryId) {
        return transactionRepository.findByCategoryCategoryId(categoryId);
    }

    public Transaction getTransactionByTransactionId(long transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }

    public void insertTransaction(Transaction transaction) {
        Set<Budget> budgets = budgetService.getAllBudgetsByDateCategoryAndAccount(transaction.getDate(),
                transaction.getCategory(), transaction.getAccount());
        transaction.setCategoryName(categoryService.getCategoryNameByCategoryId(transaction.getCategory().getCategoryId()));
        transactionRepository.save(transaction);

        if (budgets.size() != 0 && transaction.getType().equals(PaymentType.EXPENSE)) {
            for (Budget budget : budgets) {
                budget.addTransaction(transaction);
                budget.setAmount(budget.getAmount().add(transaction.getAmount()));
                budgetService.updateBudget(budget);
            }
        }
    }

    public void updateTransaction(Transaction transaction) {
        transaction.setCategoryName(categoryService.getCategoryNameByCategoryId(transaction.getCategory().getCategoryId()));
        transactionRepository.save(transaction);
    }

    @Transactional
    public void postTransaction(User user, String account, String category, String type, LocalDateTime date, String amount,
                                Transaction transaction, long transactionId) {

        Account acc = accountService.getAccountByUserAndAccountName(user, account);
        Category cat = categoryService.getCategoryByCategoryName(category);
        Transaction newTransaction = new Transaction(PaymentType.valueOf(type), transaction.getDescription(),
                BigDecimal.valueOf(Double.valueOf(amount)), acc, cat, date);
        if (transactionId != 0) {
            newTransaction.setTransactionId(transactionId);
        }
        BigDecimal newValue = BigDecimal.valueOf(Double.valueOf(amount));
        BigDecimal oldValue = accountService.getAmountByAccountId(acc.getAccountId());

        if (transactionId != 0) {
            Transaction oldTransacation = getTransactionByTransactionId(transactionId);
            if (oldTransacation.getType().equals(PaymentType.EXPENSE)) {
                acc.setAmount(oldValue.add(oldTransacation.getAmount()));
                acc.setAmount(accountService.getAmountByAccountId(acc.getAccountId()).subtract(newValue));
                accountService.updateAccount(acc);
            } else if (oldTransacation.getType().equals(PaymentType.INCOME)) {
                acc.setAmount(oldValue.subtract(oldTransacation.getAmount()));
                acc.setAmount(accountService.getAmountByAccountId(acc.getAccountId()).add(newValue));
                accountService.updateAccount(acc);
            }
            updateTransaction(newTransaction);
        } else {
            if (type.equals(EXPENSE)) {
                acc.setAmount(oldValue.subtract(newValue));
                accountService.updateAccount(acc);
            } else if (type.equals(INCOME)) {
                acc.setAmount(oldValue.add(newValue));
                accountService.updateAccount(acc);
            }
            insertTransaction(newTransaction);
        }
    }

    @Transactional
    public void deleteTransaction(User user, long transactionId) {
        Transaction transaction = this.getTransactionByTransactionId(transactionId);
        Account account = accountService.getAccountByAccountId(transaction.getAccount().getAccountId());

        BigDecimal newValue = transaction.getAmount();
        BigDecimal oldValue = accountService.getAmountByAccountId(transaction.getAccount().getAccountId());

        if (transaction.getType().equals(PaymentType.EXPENSE)) {
            account.setAmount(oldValue.add(newValue));
            accountService.updateAccount(account);
        } else if (transaction.getType().equals(PaymentType.INCOME)) {
            account.setAmount(oldValue.subtract(newValue));
            accountService.updateAccount(account);
        }

        Set<Budget> budgets = budgetService.getAllBudgetsByDateCategoryAndAccount(transaction.getDate(),
                transaction.getCategory(), transaction.getAccount());
        transactionRepository.delete(transaction);

        if (budgets.size() != 0 && transaction.getType().equals(PaymentType.EXPENSE)) {
            for (Budget budget : budgets) {
                budget.setAmount(budget.getAmount().subtract(transaction.getAmount()));
                budgetService.updateBudget(budget);
            }
        }
    }

    public boolean existsTransaction(Budget budget) {
        LocalDateTime fromDate = budget.getFromDate();
        LocalDateTime toDate = budget.getToDate();
        Category category = budget.getCategory();
        Account account = budget.getAccount();

        List<Transaction> transactions = transactionRepository.findByCategoryAndAccount(category, account);
        for (Transaction transaction : transactions) {
            PaymentType type = transaction.getType();
            LocalDateTime date = transaction.getDate();

            if (type.equals(PaymentType.EXPENSE) && isBetweenTwoDates(date, fromDate, toDate)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBetweenTwoDates(LocalDateTime date, LocalDateTime from, LocalDateTime to) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    public Set<Transaction> getAllTransactionsForBudget(Budget budget) {
        LocalDateTime fromDate = budget.getFromDate();
        LocalDateTime toDate = budget.getToDate();
        Category category = budget.getCategory();
        Account account = budget.getAccount();

        Set<Transaction> result = new HashSet<>();
        List<Transaction> transactions = transactionRepository.findByCategoryAndAccount(category, account);

        for (Transaction transaction : transactions) {
            PaymentType type = transaction.getType();
            LocalDateTime date = transaction.getDate();

            if (type.equals(PaymentType.EXPENSE) && isBetweenTwoDates(date, fromDate, toDate)) {
                result.add(transaction);
            }
        }
        return result;
    }
}
