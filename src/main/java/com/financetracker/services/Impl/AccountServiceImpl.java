package com.financetracker.services.Impl;

import com.financetracker.model.Account;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.PaymentType;
import com.financetracker.model.User;
import com.financetracker.repositories.AccountRepository;
import com.financetracker.services.AccountService;
import com.financetracker.services.CategoryService;
import com.financetracker.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AccountServiceImpl implements AccountService {

    public static final String TRANSFER = "Transfer";
    public static final String TRANSFER_TO_ACCOUNT = "Transfer to account ";
    public static final String TRANSFER_FROM_ACCOUNT = "Transfer from account ";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CategoryService categoryService;

    @Transactional
    public void insertAccount(Account account, User user) {
        account.setUser(user);
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(long accountId, User user) {
        accountRepository.delete(accountId);
    }

    public long getAccountId(User user, String name) {
        Account account = accountRepository.findByUserAndName(user, name);
        return account.getAccountId();
    }

    public Set<Account> getAllAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }

    public Account getAccountByAccountId(long accountId) {
        return accountRepository.findByAccountId(accountId);
    }

    public void updateAccount(Account account) {
        accountRepository.save(account);
    }

    @Transactional
    public void makeTransferToOtherAccount(User user, String inputFromAccount, String inputToAccount, String inputAmount) {
        BigDecimal amount = BigDecimal.valueOf(Double.valueOf(inputAmount));
        Account from = getAccountByUserAndAccountName(user, inputFromAccount);
        Account to = getAccountByUserAndAccountName(user, inputToAccount);

        BigDecimal currentAccountAmount = from.getAmount();
        BigDecimal newCurrentAccountAmount = currentAccountAmount.subtract(amount);
        from.setAmount(newCurrentAccountAmount);
        updateAccount(from);

        BigDecimal otherAccountAmount = to.getAmount();
        BigDecimal newOtherAccountAmount = otherAccountAmount.add(amount);
        to.setAmount(newOtherAccountAmount);
        updateAccount(to);

        Category transferCategory = categoryService.getCategoryByCategoryName(TRANSFER);
        Transaction t1 = new Transaction(PaymentType.EXPENSE, LocalDateTime.now(), amount, from, transferCategory);
        t1.setDescription(TRANSFER_TO_ACCOUNT + to.getName());
        Transaction t2 = new Transaction(PaymentType.INCOME, LocalDateTime.now(), amount, to, transferCategory);
        t2.setDescription(TRANSFER_FROM_ACCOUNT + from.getName());

        transactionService.insertTransaction(t1);
        transactionService.insertTransaction(t2);
    }

    public BigDecimal getAmountByAccountId(long accountId) {
        Account account = getAccountByAccountId(accountId);
        return account.getAmount();
    }

    public String getAccountNameByAccountId(long accountId) {
        Account account = getAccountByAccountId(accountId);
        return account.getName();
    }

    public Account getAccountByUserAndAccountName(User user, String name) {
        return accountRepository.findByUserAndName(user, name);
    }
}
