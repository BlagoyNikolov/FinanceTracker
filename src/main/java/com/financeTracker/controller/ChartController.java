package com.financeTracker.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.xpath.axes.DescendantIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.financeTracker.model.Account;
import com.financeTracker.model.User;
import com.financeTracker.model.db.AccountDAO;
import com.financeTracker.model.db.CategoryDAO;
import com.financeTracker.model.db.TransactionDAO;


@Controller
public class ChartController {
	@Autowired
	private CategoryDAO categoryDao;
	
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private TransactionDAO transactionDAO;

	@RequestMapping(value="/cashflowStructute", method=RequestMethod.GET)
	public String getAllReports(HttpSession session, Model model) {
		User u =  (User) session.getAttribute("user");
		
		Set<Account> accounts = new HashSet<Account>();
		Map<String, BigDecimal> transactionCategories = null;
		
		try {
			accounts = accountDAO.getAllAccountsByUserId(u.getUserId());
			transactionCategories = transactionDAO.getAllCategoriesAndTheirAmountsByUserId(u.getUserId(), "EXPENCE");
		} catch (SQLException e) {
			return "error500";
		}
		
		model.addAttribute("accounts", accounts);
		model.addAttribute("transactionsCategories", transactionCategories);
		
		return "cashflowStructute";
	}
	
	@RequestMapping(value="/getTransactions", method=RequestMethod.POST)
	public String postGetTransactions(HttpServletRequest request, HttpSession session, Model model) {
		String date = request.getParameter("date");
		String type = request.getParameter("type");
		String account = request.getParameter("account");
		
		String[] inputDate = date.split("/");
		int monthFrom = Integer.valueOf(inputDate[0]);
		int dayOfMonthFrom = Integer.valueOf(inputDate[1]);
		String[] temp = inputDate[2].toString().split(" - ");
		
		int yearFrom = Integer.valueOf(temp[0]);
		int monthTo = Integer.valueOf(temp[1]);
		int dayOfMonthTo = Integer.valueOf(inputDate[3]);
		int yearTo = Integer.valueOf(inputDate[4]);
		
		LocalDateTime dateFrom = LocalDateTime.of(yearFrom, monthFrom, dayOfMonthFrom, 0, 0, 0);
		LocalDateTime dateTo = LocalDateTime.of(yearTo, monthTo, dayOfMonthTo, 0, 0, 0);
		
		User u =  (User) session.getAttribute("user");
		
		Set<Account> accounts = new HashSet<Account>();
		TreeMap<String, BigDecimal> transactions = null;
		
		try {
			accounts = accountDAO.getAllAccountsByUserId(u.getUserId());
			transactions = transactionDAO.getAllTransactionsByUserDateTypeAccount(u.getUserId(), dateFrom, dateTo, type, account);
		} catch (SQLException e) {
			return "error500";
		}
		
		model.addAttribute("accounts", accounts);
		model.addAttribute("transactionsCategories", transactions);
		
		return "cashflowStructute";
	}
	
	@RequestMapping(value = "/incomeVsExpenses", method = RequestMethod.GET)
	public String getIncomeVsExpences(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		
		try {
			Map<String, BigDecimal> transactions = transactionDAO.getIncomeVsExpences(user.getUserId(), 0);
			
			model.addAttribute("transactions", transactions);
			model.addAttribute("accounts", accountDAO.getAllAccountsByUserId(user.getUserId()));
		} catch (SQLException e) {
			return "error500";
		}
		
		return "incomeVsExpenses";
	}
	
	@RequestMapping(value = "/incomeVsExpenses/filtered", method = RequestMethod.GET)
	public String getFilteredIncomeVsExpences(HttpSession session, Model model, HttpServletRequest request) {
		User user = (User) session.getAttribute("user");
		String date = request.getParameter("date");
		String account = request.getParameter("account");
		
		String[] inputDate = date.split("/");
		
		int monthFrom = Integer.valueOf(inputDate[0]);
		
		int dayOfMonthFrom = Integer.valueOf(inputDate[1]);
		
		String[] temp = inputDate[2].toString().split(" - ");
		
		int yearFrom = Integer.valueOf(temp[0]);
		
		int monthTo = Integer.valueOf(temp[1]);
		
		int dayOfMonthTo = Integer.valueOf(inputDate[3]);
		
		int yearTo = Integer.valueOf(inputDate[4]);
		
		LocalDateTime from = LocalDateTime.of(yearFrom, monthFrom, dayOfMonthFrom, 0, 0, 0);
		LocalDateTime to = LocalDateTime.of(yearTo, monthTo, dayOfMonthTo, 0, 0, 0);
		
		try {
			long accId = 0;
			
			if (!account.equals("All accounts")) {
				accId = accountDAO.getAccountId(user, account);
			}
			
			Map<String, BigDecimal> transactions = transactionDAO.getIncomeVsExpences(user.getUserId(), accId, from, to);
			
			model.addAttribute("transactions", transactions);
			model.addAttribute("date", date);
			model.addAttribute("accounts", accountDAO.getAllAccountsByUserId(user.getUserId()));
		} catch (SQLException e) {
			return "error500";
		}
		
		return "incomeVsExpenses";
	}
	
	@RequestMapping(value = "/cashflowTrend", method = RequestMethod.GET)
	public String getCashflowTrend(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		
		try {
			Map<LocalDate, BigDecimal> defaultTransactions = transactionDAO.getTransactionAmountAndDate(user.getUserId(), 0);
			
			Set<Account> accounts = accountDAO.getAllAccountsByUserId(user.getUserId());
			BigDecimal allBalance = new BigDecimal(0);
			for (Account account : accounts) {
				allBalance = allBalance.add(account.getAmount());
			}
			
			Map<LocalDate, BigDecimal> reverseDefaultTransactions = new TreeMap<LocalDate,BigDecimal>(Collections.reverseOrder());
			reverseDefaultTransactions.putAll(defaultTransactions);
			
			for (LocalDate date : reverseDefaultTransactions.keySet()) {
				BigDecimal transactionAmount = reverseDefaultTransactions.get(date);
				allBalance = allBalance.subtract(transactionAmount);
				reverseDefaultTransactions.put(date, transactionAmount.add(allBalance));
			}
			
			Map<LocalDate, BigDecimal> finalDefaultTransactions = new TreeMap<LocalDate,BigDecimal>();
			finalDefaultTransactions.putAll(reverseDefaultTransactions);
			
			model.addAttribute("accounts", accounts);
			model.addAttribute("defaultTransactions", finalDefaultTransactions);
		} catch (SQLException e) {
			return "error500";
		}
		
		return "cashflowTrend";
	}
	
	@RequestMapping(value = "/cashflowTrend/filtered", method = RequestMethod.GET)
	public String getCashflowTrendFiltered(HttpServletRequest request, HttpSession session, Model model) {
		String date = request.getParameter("date");
		String account = request.getParameter("account");
		
		User user = (User) session.getAttribute("user");
		
		String[] inputDate = date.split("/");
		
		int monthFrom = Integer.valueOf(inputDate[0]);
		
		int dayOfMonthFrom = Integer.valueOf(inputDate[1]);
		
		String[] temp = inputDate[2].toString().split(" - ");
		
		int yearFrom = Integer.valueOf(temp[0]);
		
		int monthTo = Integer.valueOf(temp[1]);
		
		int dayOfMonthTo = Integer.valueOf(inputDate[3]);
		
		int yearTo = Integer.valueOf(inputDate[4]);
		
		LocalDateTime from = LocalDateTime.of(yearFrom, monthFrom, dayOfMonthFrom, 0, 0, 0);
		LocalDateTime to = LocalDateTime.of(yearTo, monthTo, dayOfMonthTo, 0, 0, 0);
		
		Map<LocalDate, BigDecimal> defaultTransactions = new TreeMap<>();
		
		try {
			Set<Account> accounts = accountDAO.getAllAccountsByUserId(user.getUserId());
			
			Map<LocalDate, BigDecimal> finalDefaultTransactions = new TreeMap<LocalDate,BigDecimal>();
			if (account.equals("All accounts")) {
				defaultTransactions = transactionDAO.getTransactionAmountAndDate(user.getUserId(), 0, from, to);
				
				BigDecimal allBalance = new BigDecimal(0);
				for (Account acc : accounts) {
					allBalance = allBalance.add(acc.getAmount());
				}
				
				Map<LocalDate, BigDecimal> reverseDefaultTransactions = new TreeMap<LocalDate,BigDecimal>(Collections.reverseOrder());
				reverseDefaultTransactions.putAll(defaultTransactions);
				
				for (LocalDate tdate : reverseDefaultTransactions.keySet()) {
					BigDecimal transactionAmount = reverseDefaultTransactions.get(tdate);
					allBalance = allBalance.subtract(transactionAmount);
					reverseDefaultTransactions.put(tdate, transactionAmount.add(allBalance));
				}
				
				finalDefaultTransactions.putAll(reverseDefaultTransactions);
			} else {
				long accId = accountDAO.getAccountId(user, account);
				Account acc = accountDAO.getAccountByAccountId(accId);
				BigDecimal accVal = acc.getAmount();
				
				defaultTransactions = transactionDAO.getTransactionAmountAndDate(user.getUserId(), accId, from, to);
				
				Map<LocalDate, BigDecimal> reverseDefaultTransactions = new TreeMap<LocalDate,BigDecimal>(Collections.reverseOrder());
				reverseDefaultTransactions.putAll(defaultTransactions);
				
				for (LocalDate tdate : reverseDefaultTransactions.keySet()) {
					BigDecimal transactionAmount = reverseDefaultTransactions.get(tdate);
					accVal = accVal.subtract(transactionAmount);
					reverseDefaultTransactions.put(tdate, transactionAmount.add(accVal));
				}
				
				finalDefaultTransactions.putAll(reverseDefaultTransactions);
			}
			
			model.addAttribute("accounts", accounts);
			model.addAttribute("defaultTransactions", finalDefaultTransactions);
			model.addAttribute("date", date);
		} catch (SQLException e) {
			return "error500";
		}
		
		return "cashflowTrend";
	}
}
