package com.financetracker.controller;

import com.financetracker.model.Account;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.services.AccountService;
import com.financetracker.services.CategoryService;
import com.financetracker.services.TransactionService;
import com.financetracker.util.DateConverters;
import com.financetracker.util.TransactionComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping(value = "/account")
public class TransactionController {
    public static final String COULD_NOT_MAKE_TRANSFER_MESSAGE = "Could not make transfer. Please, enter valid data!";
    public static final String COULD_NOT_UPDATE_TRANSACTION_MESSAGE = "Could not update transaction. Please, enter valid data!";
    public static final String COULD_NOT_INSERT_TRANSACTION_MESSAGE = "Could not insert transaction. Please, enter valid data!";

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(value = "/{accountId}", method = RequestMethod.GET)
    public String getAllTransactions(HttpServletRequest request, HttpSession session, @PathVariable("accountId") Long accountId, Model model) {
        User user = (User) session.getAttribute("user");
        TreeSet<Transaction> transactions = new TreeSet<>(new TransactionComparator());
        Set<Category> allCategories = new HashSet<Category>();
        request.getSession().setAttribute("accountId", accountId);
        transactions.addAll(transactionService.getAllTransactionsByAccountId(accountId));
        BigDecimal accountBalance = accountService.getAmountByAccountId(accountId);
        Set<Category> categories = categoryService.getAllCategoriesByUserId();
        Set<Category> ownCategories = categoryService.getAllCategoriesByUserId(user.getUserId());
        allCategories.addAll(categories);
        allCategories.addAll(ownCategories);
        Set<Account> accounts = accountService.getAllAccountsByUser(user);
        String accountName = accountService.getAccountNameByAccountId(accountId);
        String balance = NumberFormat.getCurrencyInstance(Locale.US).format(accountBalance);
        List<Transaction> transactionsPaged = transactionService.getPagingTransactions(accountId, 1);

        int allCount = transactionService.getAllTransactionsByAccountId(accountId).size();
        int pages = (int) Math.ceil(allCount / (double) 10);

        model.addAttribute("pages", pages);
        model.addAttribute("pagedTransactions", transactionsPaged);
        model.addAttribute("accountId", accountId);
        request.getSession().setAttribute("categories", allCategories);
        request.getSession().setAttribute("accounts", accounts);
        request.getSession().setAttribute("accountName", accountName);
        request.getSession().setAttribute("balance", balance);
        request.getSession().setAttribute("transactions", transactions);

        return "transactions";
    }

    @RequestMapping(value = "/addTransaction", method = RequestMethod.GET)
    public String getAddTransaction(HttpSession session, Model model) {
        Transaction transaction = new Transaction();
        session.setAttribute("link", "account/addTransaction");
        model.addAttribute("transaction", transaction);
        return "addTransaction";
    }

    @RequestMapping(value = "/addTransaction", method = RequestMethod.POST)
    public String postAddTransaction(HttpServletRequest request, Model model,
                                     @Valid @ModelAttribute("transaction") Transaction transaction, BindingResult bindingResult) {
        String type = request.getParameter("type");
        String account = request.getParameter("account");
        String category = request.getParameter("category");
        String amount = request.getParameter("amount");
        String[] tags = request.getParameterValues("tagss");

        if (type.isEmpty() || account.isEmpty() || category.isEmpty() || bindingResult.hasErrors()) {
            model.addAttribute("error", COULD_NOT_INSERT_TRANSACTION_MESSAGE);
            Transaction defaultTransaction = new Transaction();
            return "addTransaction";
        }

        User user = (User) request.getSession().getAttribute("user");
        transactionService.postTransaction(user, account, category, type, LocalDateTime.now(), amount, transaction, 0);
        Account acc = accountService.getAccountByUserAndAccountName(user, account);

        return "redirect:/account/" + acc.getAccountId();
    }

    @RequestMapping(value = "/transaction/{transactionId}", method = RequestMethod.GET)
    public String getEditTransaction(HttpSession session, Model model, @PathVariable("transactionId") Long transactionId) {
        Transaction transaction = transactionService.getTransactionByTransactionId(transactionId);
        String type = transaction.getType().toString();
        String description = transaction.getDescription();
        BigDecimal amount = transaction.getAmount();
        String accountName = accountService.getAccountNameByAccountId(transaction.getAccount().getAccountId());
        String category = categoryService.getCategoryNameByCategoryId(transaction.getCategory().getCategoryId());
        LocalDateTime date = transaction.getDate();

        model.addAttribute("transaction", transaction);
        model.addAttribute("editTransactionType", type);
        model.addAttribute("editTransactionDescription", description);
        model.addAttribute("editTransactionAmount", amount);
        model.addAttribute("editTransactionAccount", accountName);
        model.addAttribute("editTransactionCategory", category);
        model.addAttribute("editTransactionDate", date);

        session.setAttribute("link", "account/transaction/" + transactionId);
        session.setAttribute("transactionId", transactionId);

        return "editTransaction";
    }

    @RequestMapping(value = "/transaction/editTransaction", method = RequestMethod.POST)
    public String postEditTransaction(HttpServletRequest request, HttpSession session, Model model,
                                      @Valid @ModelAttribute("transaction") Transaction transaction, BindingResult bindingResult) {
        String type = request.getParameter("type");
        String account = request.getParameter("account");
        String category = request.getParameter("category");
        String amount = request.getParameter("amount");
        String date = request.getParameter("date");
        long transactionId = (long) request.getSession().getAttribute("transactionId");

        if (type.isEmpty() || account.isEmpty() || category.isEmpty() || bindingResult.hasErrors()) {
            model.addAttribute("error", COULD_NOT_UPDATE_TRANSACTION_MESSAGE);
            Transaction defaultTransaction = new Transaction();

            LocalDateTime newDate = DateConverters.convertFromStringToLocalDateTime(date);
            model.addAttribute("editTransactionType", type);
            model.addAttribute("editTransactionDescription", transaction.getDescription());
            model.addAttribute("editTransactionAmount", amount);
            model.addAttribute("editTransactionAccount", account);
            model.addAttribute("editTransactionCategory", category);
            model.addAttribute("editTransactionDate", newDate);

            return "editTransaction";
        }

        LocalDateTime newDate = DateConverters.convertFromStringToLocalDateTime(date);
        User user = (User) session.getAttribute("user");
        transactionService.postTransaction(user, account, category, type, newDate, amount, transaction, transactionId);
        Account acc = accountService.getAccountByUserAndAccountName(user, account);

        return "redirect:/account/" + acc.getAccountId();
    }

    @RequestMapping(value = "/transfer/accountId/{accountId}", method = RequestMethod.GET)
    public String getTransfer(Model model, HttpSession session, @PathVariable("accountId") Long originAccountId) {
        User user = (User) session.getAttribute("user");
        Account originAccount = accountService.getAccountByAccountId(originAccountId);
        Set<Account> userAccounts = accountService.getAllAccountsByUser(user);
        model.addAttribute("firstAccount", originAccount);
        model.addAttribute("userAccounts", userAccounts);

        return "transfer";
    }

    @RequestMapping(value = "/transfer/accountId/transfer", method = RequestMethod.POST)
    public String postTransfer(HttpServletRequest request, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        String amountParam = request.getParameter("amount");
        String fromAccount = request.getParameter("fromAccount");
        String toAccount = request.getParameter("toAccount");

        if (amountParam.isEmpty() || fromAccount.equals(toAccount)
                || BigDecimal.valueOf(Double.valueOf(amountParam)).compareTo(BigDecimal.ZERO) < 0
                || BigDecimal.valueOf(Double.valueOf(amountParam)).compareTo(BigDecimal.ZERO) == 0) {
            model.addAttribute("error", COULD_NOT_MAKE_TRANSFER_MESSAGE);

            Account originAccount = accountService.getAccountByUserAndAccountName(user, fromAccount);
            Set<Account> userAccounts = accountService.getAllAccountsByUser(user);

            model.addAttribute("firstAccount", originAccount);
            model.addAttribute("userAccounts", userAccounts);

            return "transfer";
        }

        Account from = accountService.getAccountByUserAndAccountName(user, fromAccount);
        accountService.makeTransferToOtherAccount(user, fromAccount, toAccount, amountParam);

        return "redirect:/account/" + from.getAccountId();
    }

    @RequestMapping(value = "transaction/deleteTransaction/{transactionId}", method = RequestMethod.POST)
    public String deleteTransaction(@PathVariable("transactionId") Long transactionId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Transaction transaction = transactionService.getTransactionByTransactionId(transactionId);
        transactionService.deleteTransaction(user, transactionId);
        return "redirect:/account/" + transaction.getAccount().getAccountId();
    }

    @GetMapping(value = "/{accountId}/{page}")
    public String transactionPaging(@PathVariable("accountId") Long accountId, @PathVariable("page") int page, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<Transaction> transactionsPaged = transactionService.getPagingTransactions(accountId, page);
        int allCount = transactionService.getAllTransactionsByAccountId(accountId).size();
        int pages = (int) Math.ceil(allCount / (double) 10);
        String accountName = accountService.getAccountNameByAccountId(accountId);
        BigDecimal accountBalance = accountService.getAmountByAccountId(accountId);
        String balance = NumberFormat.getCurrencyInstance(Locale.US).format(accountBalance);
        Set<Account> accounts = accountService.getAllAccountsByUser(user);

        model.addAttribute("accounts", accounts);
        model.addAttribute("accountName", accountName);
        model.addAttribute("balance", balance);
        model.addAttribute("pagedTransactions", transactionsPaged);
        model.addAttribute("accountId", accountId);
        model.addAttribute("pages", pages);
        return "transactions";
    }
}
