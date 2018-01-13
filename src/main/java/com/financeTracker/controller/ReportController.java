package com.financetracker.controller;

import com.financetracker.model.Account;
import com.financetracker.model.Transaction;
import com.financetracker.model.User;
import com.financetracker.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Set;
import java.util.TreeSet;

@Controller
public class ReportController {

    @Autowired
    private ReportService reportService;

    @RequestMapping(value = "/reports", method = RequestMethod.GET)
    public String getAllReports(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        Set<Account> allAccounts = new TreeSet<>((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        TreeSet<Transaction> allTransactions = reportService.getAllReportTransactions(user, allAccounts);
        model.addAttribute("allAccounts", allAccounts);
        model.addAttribute("allTransactions", allTransactions);
        return "reports";
    }

    @RequestMapping(value = "/reports/filtered", method = RequestMethod.GET)
    public String filterTransactions(HttpServletRequest request, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        String categoryName = request.getParameter("category");
        String type = request.getParameter("type");
        String accName = request.getParameter("account");
        String date = request.getParameter("date");
        Set<Account> allAccounts = new TreeSet<>((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        TreeSet<Transaction> transactions = reportService.getFilteredReportTransactions(user, categoryName, type, accName, date, allAccounts);
        model.addAttribute("date", date);
        model.addAttribute("allAccounts", allAccounts);
        model.addAttribute("allTransactions", transactions);
        return "reports";
    }
}
