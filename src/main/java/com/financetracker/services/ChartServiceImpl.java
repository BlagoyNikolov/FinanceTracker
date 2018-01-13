package com.financetracker.services;

import com.financetracker.model.Account;
import com.financetracker.model.Category;
import com.financetracker.model.PaymentType;
import com.financetracker.model.TransactionVisualizer;
import com.financetracker.model.User;
import com.financetracker.util.DateConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ChartServiceImpl implements ChartService {

    public static final String ALL_ACCOUNTS = "All accounts";
    public static final String INCOME = "INCOME";
    public static final String EXPENSE = "EXPENSE";

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryService categoryService;

    public TreeSet<Account> getAllAccounts(User user) {
        TreeSet<Account> accounts = new TreeSet<>((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        accounts.addAll(accountService.getAllAccountsByUser(user));
        return accounts;
    }

    public BigDecimal calculateAllBalance(Set<Account> accounts) {
        BigDecimal allBalance = BigDecimal.valueOf(0);
        for (Account account : accounts) {
            allBalance = allBalance.add(account.getAmount());
        }
        return allBalance;
    }

    public Map<String, BigDecimal> getCashFlowStructure(User user) {
        TreeMap<String, BigDecimal> result = new TreeMap<>();
        Set<Category> categories = categoryService.getAllCategoriesByType(user, PaymentType.EXPENSE);

        categories.stream().filter(cat -> cat.getTransactions().size() != 0).forEach(cat -> {
            String categoryName = cat.getName();
            BigDecimal sum = cat.getTransactions()
                .stream()
                .filter(tr -> (tr.getAccount().getUser().getUserId() == user.getUserId()))
                .map(tr -> tr.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

            if (sum != BigDecimal.ZERO) {
                result.put(categoryName, sum);
            }
        });
        return result;
    }

    public TreeMap<String, BigDecimal> getFilteredCashFlowStructure(User user, LocalDateTime from, LocalDateTime to,
                                                                    String type, String account) {
        TreeMap<String, BigDecimal> result = new TreeMap<>();
        Set<Category> categories = categoryService.getAllCategoriesByType(user, PaymentType.valueOf(type));

        categories.stream().filter(cat -> cat.getTransactions().size() != 0).forEach(cat -> {
            String categoryName = cat.getName();

            BigDecimal sum;
            if (account.equals(ALL_ACCOUNTS)) {
                sum = cat.getTransactions()
                    .stream()
                    .filter(tr -> (tr.getAccount().getUser().getUserId() == user.getUserId()))
                    .filter(transaction -> (transaction.getDate().isAfter(from) && transaction.getDate().isBefore(to)))
                    .map(tr -> tr.getAmount())
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            } else {
                sum = cat.getTransactions()
                    .stream()
                    .filter(tr -> (tr.getAccount().getUser().getUserId() == user.getUserId()))
                    .filter(transaction -> (transaction.getDate().isAfter(from) && transaction.getDate().isBefore(to)))
                    .filter(transaction -> (transaction.getAccount().getName().equals(account)))
                    .map(tr -> tr.getAmount())
                    .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            }
            if (sum != BigDecimal.ZERO) {
                result.put(categoryName, sum);
            }
        });
        return result;
    }

    public Map<String, BigDecimal> getIncomeVsExpenses(User user) {
        Map<String, BigDecimal> result = new HashMap<>();
        result.put(INCOME, BigDecimal.valueOf(0));
        result.put(EXPENSE, BigDecimal.valueOf(0));
        Set<Account> accounts = accountService.getAllAccountsByUser(user);

        accounts.stream().filter(acc -> acc.getTransactions().size() != 0).forEach(acc -> {
            BigDecimal incomeSum = acc.getTransactions()
                .stream()
                .filter(transaction -> (transaction.getType().equals(PaymentType.INCOME)))
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            result.put(INCOME, result.get(INCOME).add(incomeSum));

            BigDecimal expenseSum = acc.getTransactions()
                .stream()
                .filter(transaction -> (transaction.getType().equals(PaymentType.EXPENSE)))
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            result.put(EXPENSE, result.get(EXPENSE).add(expenseSum));
        });

        return result;
    }

    public Map<String, BigDecimal> getFileteredIncomeVsExpenses(User user, long accountId, LocalDateTime from, LocalDateTime to) {
        Map<String, BigDecimal> result = new HashMap<>();
        result.put(INCOME, BigDecimal.valueOf(0));
        result.put(EXPENSE, BigDecimal.valueOf(0));
        Set<Account> accounts = accountService.getAllAccountsByUser(user);

        accounts.stream().filter(acc -> acc.getTransactions().size() != 0).forEach(acc -> {
            BigDecimal incomeSum = acc.getTransactions()
                .stream()
                .filter(transaction -> (transaction.getType().equals(PaymentType.INCOME)))
                .filter(transaction -> (accountId == 0) || (transaction.getAccount().getAccountId() == accountId))
                .filter(transaction -> transaction.getDate().isAfter(from) && transaction.getDate().isBefore(to))
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            result.put(INCOME, result.get(INCOME).add(incomeSum));

            BigDecimal expenseSum = acc.getTransactions()
                .stream()
                .filter(transaction -> (transaction.getType().equals(PaymentType.EXPENSE)))
                .filter(transaction -> (accountId == 0) || (transaction.getAccount().getAccountId() == accountId))
                .filter(transaction -> transaction.getDate().isAfter(from) && transaction.getDate().isBefore(to))
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            result.put(EXPENSE, result.get(EXPENSE).add(expenseSum));
        });

        return result;
    }

    public Map<LocalDate, BigDecimal> getTransactionAmountAndDate(User user) {
        Map<LocalDate, BigDecimal> result = new TreeMap<>();
        Set<Account> accounts = accountService.getAllAccountsByUser(user);

        accounts.stream().filter(acc -> acc.getTransactions().size() != 0).forEach(acc -> {
            Map<LocalDate, BigDecimal> map = acc.getTransactions()
                .stream()
                .map(transaction -> new TransactionVisualizer(transaction))
                .collect(
                        Collectors.groupingBy(TransactionVisualizer::getDate,
                                Collectors.mapping(TransactionVisualizer::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
            for (Map.Entry<LocalDate, BigDecimal> entry : map.entrySet()) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                } else {
                    result.put(entry.getKey(), result.get(entry.getKey()).add(entry.getValue()));
                }
            }
        });
        return result;
    }

    public Map<LocalDate, BigDecimal> getFilteredTransactionAmountAndDate(User user, long accountId, LocalDateTime from, LocalDateTime to) {
        Map<LocalDate, BigDecimal> result = new TreeMap<>();
        Set<Account> accounts = new HashSet<>();
        if (accountId == 0) {
            accounts = accountService.getAllAccountsByUser(user);
        } else {
            accounts.add(accountService.getAccountByAccountId(accountId));
        }

        accounts.stream().filter(acc -> acc.getTransactions().size() != 0).forEach(acc -> {
            Map<LocalDate, BigDecimal> map = acc.getTransactions()
                .stream()
                .filter(transaction -> transaction.getDate().isAfter(from) && transaction.getDate().isBefore(to))
                .map(transaction -> new TransactionVisualizer(transaction))
                .collect(
                        Collectors.groupingBy(TransactionVisualizer::getDate,
                                Collectors.mapping(TransactionVisualizer::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
            for (Map.Entry<LocalDate, BigDecimal> entry : map.entrySet()) {
                if (!result.containsKey(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                } else {
                    result.put(entry.getKey(), result.get(entry.getKey()).add(entry.getValue()));
                }
            }
        });
        return result;
    }

    public Map<LocalDate, BigDecimal> getGraphData(User user, BigDecimal allBalance) {
        Map<LocalDate, BigDecimal> defaultTransactions = getTransactionAmountAndDate(user);
        Map<LocalDate, BigDecimal> reverseDefaultTransactions = new TreeMap<LocalDate, BigDecimal>(Collections.reverseOrder());
        reverseDefaultTransactions.putAll(defaultTransactions);

        for (LocalDate date : reverseDefaultTransactions.keySet()) {
            BigDecimal transactionAmount = reverseDefaultTransactions.get(date);
            allBalance = allBalance.subtract(transactionAmount);
            reverseDefaultTransactions.put(date, transactionAmount.add(allBalance));
        }

        Map<LocalDate, BigDecimal> finalDefaultTransactions = new TreeMap<LocalDate, BigDecimal>();
        finalDefaultTransactions.putAll(reverseDefaultTransactions);

        return finalDefaultTransactions;
    }

    public Map<LocalDate, BigDecimal> getFilteredGraphData(User user, String date, String account, Set<Account> accounts) {
        LocalDateTime[] dateRange = DateConverters.dateRange(date);
        Map<LocalDate, BigDecimal> defaultTransactions = new TreeMap<>();

        Map<LocalDate, BigDecimal> finalDefaultTransactions = new TreeMap<LocalDate, BigDecimal>();
        if (account.equals(ALL_ACCOUNTS)) {
            defaultTransactions = getFilteredTransactionAmountAndDate(user, 0, dateRange[0], dateRange[1]);

            BigDecimal allBalance = new BigDecimal(0);
            for (Account acc : accounts) {
                allBalance = allBalance.add(acc.getAmount());
            }

            Map<LocalDate, BigDecimal> reverseDefaultTransactions = new TreeMap<LocalDate, BigDecimal>(Collections.reverseOrder());
            reverseDefaultTransactions.putAll(defaultTransactions);

            for (LocalDate transactionDate : reverseDefaultTransactions.keySet()) {
                BigDecimal transactionAmount = reverseDefaultTransactions.get(transactionDate);
                allBalance = allBalance.subtract(transactionAmount);
                reverseDefaultTransactions.put(transactionDate, transactionAmount.add(allBalance));
            }

            finalDefaultTransactions.putAll(reverseDefaultTransactions);
        } else {
            long accId = accountService.getAccountId(user, account);
            Account acc = accountService.getAccountByAccountId(accId);
            BigDecimal accVal = acc.getAmount();

            defaultTransactions = getFilteredTransactionAmountAndDate(user, accId, dateRange[0], dateRange[1]);

            Map<LocalDate, BigDecimal> reverseDefaultTransactions = new TreeMap<LocalDate, BigDecimal>(Collections.reverseOrder());
            reverseDefaultTransactions.putAll(defaultTransactions);

            for (LocalDate tdate : reverseDefaultTransactions.keySet()) {
                BigDecimal transactionAmount = reverseDefaultTransactions.get(tdate);
                accVal = accVal.subtract(transactionAmount);
                reverseDefaultTransactions.put(tdate, transactionAmount.add(accVal));
            }

            finalDefaultTransactions.putAll(reverseDefaultTransactions);
        }
        return finalDefaultTransactions;
    }
}