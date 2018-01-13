package com.financetracker.services;

import com.financetracker.model.Account;
import com.financetracker.model.Category;
import com.financetracker.model.PaymentType;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.util.DateConverters;
import com.financetracker.util.TransactionComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    public static final String ALL_CATEGORIES = "All categories";
    public static final String ALL_ACCOUNTS = "All accounts";
    public static final String ALL_TYPES = "All types";

    @Autowired
    CategoryService categoryService;

    @Autowired
    AccountService accountService;

    public TreeSet<Transaction> getAllReportTransactions(User user, Set<Account> allAccounts) {
        TreeSet<Transaction> allTransactions = new TreeSet<>(new TransactionComparator());
        for (Account acc : accountService.getAllAccountsByUser(user)) {
            allAccounts.add(acc);
            allTransactions.addAll(acc.getTransactions());
        }

        return allTransactions;
    }

    public TreeSet<Transaction> getFilteredReportTransactions(User user, String categoryName, String type, String accName, String date,
                                                              Set<Account> allAccounts) {
        LocalDateTime[] dateRange = DateConverters.dateRange(date);
        TreeSet<Transaction> transactions = new TreeSet<>(new TransactionComparator());
        transactions.addAll(getReportTransactions(user, type,
            categoryName.equals(ALL_CATEGORIES) ? 0 : categoryService.getCategoryByCategoryName(categoryName).getCategoryId(),
            accName.equals(ALL_ACCOUNTS) ? 0 : accountService.getAccountId(user, accName), dateRange[0], dateRange[1]));

        for (Account acc : accountService.getAllAccountsByUser(user)) {
            allAccounts.add(acc);
        }
        return transactions;
    }

    public Set<Transaction> getReportTransactions(User user, String type, long categoryId, long accountId,
                                                  LocalDateTime from, LocalDateTime to) {
        Set<Transaction> result = new HashSet<>();
        Set<Category> categories = new HashSet<>();

        if (categoryId == 0) {
            if (type.equals(ALL_TYPES)) {
                Set<Category> defaultCategories = categoryService.getAllCategoriesByUserId();
                Set<Category> ownCategories = categoryService.getAllCategoriesByUserId(user.getUserId());
                categories.addAll(defaultCategories);
                categories.addAll(ownCategories);
            } else {
                categories = categoryService.getAllCategoriesByType(user, PaymentType.valueOf(type));
            }
        } else {
            categories.add(categoryService.getCategoryByCategoryId(categoryId));
        }

        if (accountId == 0) {
            categories.stream().filter(cat -> cat.getTransactions().size() != 0).forEach(cat -> {
                Set<Transaction> transactions = cat.getTransactions()
                    .stream()
                    .filter(tr -> (tr.getAccount().getUser().getUserId() == user.getUserId()))
                    .filter(transaction -> (transaction.getDate().isAfter(from) && transaction.getDate().isBefore(to)))
                    .collect(Collectors.toSet());
                result.addAll(transactions);
            });
        } else {
            categories.stream().filter(cat -> cat.getTransactions().size() != 0).forEach(cat -> {
                Set<Transaction> transactions = cat.getTransactions()
                    .stream()
                    .filter(tr -> (tr.getAccount().getUser().getUserId() == user.getUserId()))
                    .filter(transaction -> (transaction.getDate().isAfter(from) && transaction.getDate().isBefore(to)))
                    .filter(transaction -> (transaction.getAccount().getName().equals(accountService.getAccountNameByAccountId(accountId))))
                    .collect(Collectors.toSet());
                result.addAll(transactions);
            });
        }
        return result;
    }
}
