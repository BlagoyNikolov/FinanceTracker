package com.financeTracker.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.financeTracker.model.Account;
import com.financeTracker.model.Budget;
import com.financeTracker.model.Category;
import com.financeTracker.model.Tag;
import com.financeTracker.model.Transaction;
import com.financeTracker.model.User;
import com.financeTracker.model.db.AccountDAO;
import com.financeTracker.model.db.BudgetDAO;
import com.financeTracker.model.db.BudgetsHasTransactionsDAO;
import com.financeTracker.model.db.CategoryDAO;
import com.financeTracker.model.db.TagDAO;
import com.financeTracker.model.db.TransactionDAO;
import com.financeTracker.model.db.UserDAO;

@Controller
public class BudgetController {
	
	@Autowired
	private CategoryDAO categoryDao;
	
	@Autowired
	BudgetDAO budgetDao;
	
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private BudgetsHasTransactionsDAO budgetsHasTransactionsDAO;
	
	@Autowired
	private TagDAO tagDAO;
	
	@Autowired
	private TransactionDAO transactionDAO;
	
	@Autowired
	private UserDAO userDao;
	
	@RequestMapping(value="/budgets", method=RequestMethod.GET)
	public String getAllBudgets(HttpSession session, Model model) {
		User u = (User) session.getAttribute("user");
		
		Set<Budget> budgets = null;
		BigDecimal percent = new BigDecimal(0.0);
		
		Map<Budget, BigDecimal> map = new TreeMap<>((b1, b2) -> {
			if(b2.getFromDate().compareTo(b1.getFromDate()) == 0) {
				return Long.compare(b2.getBudgetId() , b1.getBudgetId());
			}
			
			return b2.getFromDate().compareTo(b1.getFromDate());
		});
		
		try {
			budgets = budgetDao.getAllBudgetsByUserId(u.getUserId());
			
			for (Budget budget : budgets) {
				percent = budget.getAmount().divide(budget.getInitialAmount(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
				
				map.put(budget, percent);
			}
		} catch (SQLException e) {
			return "error500";
		}
		
		model.addAttribute("budgets", map);
		
		return "budgets";
	}
	
	@RequestMapping(value="/addBudget", method=RequestMethod.GET)
	public String mskeBudget(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		
		try {
			Set<Account> accounts = accountDAO.getAllAccountsByUserId(user.getUserId());
			Set<String> categories = categoryDao.getAllCategoriesByType(user.getUserId(), "EXPENCE");
			Set<Tag> tags = tagDAO.getAllTagsByUserId(user.getUserId());
			
			model.addAttribute("accounts", accounts);
			model.addAttribute("categories", categories);
			model.addAttribute("tags", tags);
			model.addAttribute("budget", new Budget());
		} catch (SQLException e) {
			return "error500";
		}
		
		return "addBudget";
	}
	
	@RequestMapping (value ="/addBudget", method = RequestMethod.POST)
	public String addBudget(HttpServletRequest request, HttpSession session, Model model, @Valid @ModelAttribute("budget") Budget budget, BindingResult bindingResult) {
		User user = (User) session.getAttribute("user");

		budget.setTags(null);
		budget.setAmount(BigDecimal.valueOf(0));
		
		if (bindingResult.hasErrors()) {
			Set<Account> accounts;
			try {
				accounts = accountDAO.getAllAccountsByUserId(user.getUserId());
				Set<String> categories = categoryDao.getAllCategoriesByType(user.getUserId(), "EXPENCE");
				Set<Tag> tags = tagDAO.getAllTagsByUserId(user.getUserId());

				model.addAttribute("accounts", accounts);
				model.addAttribute("categories", categories);
				model.addAttribute("tags", tags);
				
				model.addAttribute("addBudget", "Could not create budget. Please, enter a valid data!");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				
				return "error500";
			}
			
			
			return "addBudget";
		}
		
		
		try {
			Account acc = accountDAO.getAccountByUserIDAndAccountName(user.getUserId(), request.getParameter("account"));
			Category category = categoryDao.getCategoryByCategoryName(request.getParameter("category"));
			String[] tags = request.getParameterValues("tagss");
			String date = request.getParameter("date");
			
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
			
			Set<Tag> tagsSet = new HashSet<>();
			if (tags != null) {
				for (String tagName : tags) {
					tagsSet.add(tagDAO.getTagByNameAndUser(tagName, user.getUserId()));
				}
			}

			session.setAttribute("link", "addBudget");
			budget.setFromDate(dateFrom);
			budget.setToDate(dateTo);
			budget.setAccountId(acc.getAccountId());
			budget.setCategoryId(category.getCategoryId());
			budget.setTags(tagsSet);
			
			
			budgetDao.insertBudget(budget);

			user.setLastFill(LocalDateTime.now());
			userDao.updateUser(user);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		return "redirect:/budgets";
	}
	
	@RequestMapping (value ="/budgets/{budgetId}", method = RequestMethod.GET)
	public String viewBudget(@PathVariable("budgetId") Long budgetId, Model model) {
		try {
			Budget b = budgetDao.getBudgetByBudgetId(budgetId);
			
			TreeSet<Transaction> transactions = new TreeSet<>((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
			
			for (Transaction transaction : b.getTransactions()) {
				transaction.setCategoryName(categoryDao.getCategoryNameByCategoryId(transaction.getCategory()));
				
				transactions.add(transaction);
			}
			
			model.addAttribute("budgetId", budgetId);
			model.addAttribute("budgetTransactions", transactions);
		} catch (SQLException e) {
			return "error500";
		}
		
		return "budgetInfo";
	}
	
	@RequestMapping (value ="/budgets/{budgetId}/editBudget", method = RequestMethod.POST)
	public String postEditBudget(Model model,HttpSession session, HttpServletRequest request, @PathVariable("budgetId") Long budgetId,
			@Valid @ModelAttribute("newBudget") Budget budget, BindingResult bindingResult) {
		
		User user = (User) session.getAttribute("user");
		budget.setTags(null);
		budget.setAmount(BigDecimal.valueOf(0));
		if (bindingResult.hasErrors()) {
			try {
				Budget b = budget = budgetDao.getBudgetByBudgetId(budgetId);
				
				Account acc = accountDAO.getAccountByAccountId(b.getAccountId());
				Set<Account> accounts = accountDAO.getAllAccountsByUserId(user.getUserId());
				BigDecimal amount = b.getInitialAmount();
				String categoryName = categoryDao.getCategoryNameByCategoryId(b.getCategoryId());
				Set<String> categories = categoryDao.getAllCategoriesByType(user.getUserId(), "EXPENCE");
				
				Set<Tag> tags = tagDAO.getAllTagsByUserId(user.getUserId());
				LocalDateTime fromDate = b.getFromDate();
				LocalDateTime toDate = b.getToDate();
				
				StringBuilder date = new StringBuilder();
				
				date.append(fromDate.getMonthValue()).append("/").append(fromDate.getDayOfMonth()).append("/").append(fromDate.getYear());
				date.append(" - ");
				date.append(toDate.getMonthValue()).append("/").append(toDate.getDayOfMonth()).append("/").append(toDate.getYear());
				
				Set<String> tagNames = new HashSet<String>();
				for (Tag tag : tags) {
					tagNames.add(tag.getName());
				}
				
				model.addAttribute("categoryName", categoryName);
				model.addAttribute("categories", categories);
				model.addAttribute("tagNames", tagNames);
				model.addAttribute("tags", tags);
				model.addAttribute("editBudgetAmount", amount);
				model.addAttribute("accounts", accounts);
				model.addAttribute("accountName", acc.getName());
				model.addAttribute("budget", b);
				model.addAttribute("date", date);
				model.addAttribute("newBudget", new Budget());
				
				model.addAttribute("editBudget", "Could not edit budget. Please, enter a valid data!");
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				
				return "error500";
			}
			
			
			return "editBudget";
		}
		
		try {
			Budget oldBudget = budgetDao.getBudgetByBudgetId(budgetId);
			
			Account acc = accountDAO.getAccountByUserIDAndAccountName(user.getUserId(), request.getParameter("account"));
			Category category = categoryDao.getCategoryByCategoryName(request.getParameter("category"));
			String[] tags = request.getParameterValues("tagss");
			String date = request.getParameter("date");
			
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
			
			Set<Tag> tagsSet = new HashSet<>();
			if (tags != null) {
				for (String tagName : tags) {
					tagsSet.add(tagDAO.getTagByNameAndUser(tagName, user.getUserId()));
				}
			}

			Budget newBudget = new Budget(budget.getName(), budget.getInitialAmount(), dateFrom, dateTo, acc.getAccountId(), category.getCategoryId(), tagsSet);
			newBudget.setBudgetId(budgetId);
			
			boolean exist = newBudget.getCategoryId() != oldBudget.getCategoryId() || newBudget.getAccountId() != oldBudget.getAccountId()
					|| newBudget.getFromDate() != oldBudget.getFromDate() || newBudget.getToDate() != oldBudget.getToDate();
			
			
			if (exist) {
				
				Set<Transaction> transactions = budgetsHasTransactionsDAO.getAllTransactionsByBudgetId(budgetId);
				BigDecimal newAmount = new BigDecimal(0.0);
				
				for (Transaction transaction : transactions) {
					newAmount = newAmount.subtract(transaction.getAmount());
				}
				
				newBudget.setAmount(newAmount);
				
				budgetsHasTransactionsDAO.deleteTransactionBudgetByBudgetId(budgetId);
				
				boolean exits = transactionDAO.existsTransaction(newBudget.getFromDate(), newBudget.getToDate(), newBudget.getCategoryId(), newBudget.getAccountId());
				
				if (exits) {
					transactions = transactionDAO.getAllTransactionsForBudget(newBudget.getFromDate(), newBudget.getToDate(), newBudget.getCategoryId(), newBudget.getAccountId());
				
					 newAmount = new BigDecimal(0.0);
					
					for (Transaction transaction : transactions) {
						budgetsHasTransactionsDAO.insertTransactionBudget(newBudget.getBudgetId(), transaction.getTransactionId());
						
						newAmount = newAmount.add(transaction.getAmount());
					}
					
					newBudget.setAmount(newAmount);
					newBudget.setTransactions(transactions);
					
					budgetDao.updateBudget(newBudget);
				}

				tagDAO.deleteAllTagsForBydget(budgetId);
				
				for (Tag tag : tagsSet) {
					tagDAO.insertTagToBudget(newBudget, tag);
				}
				
			}

			user.setLastFill(LocalDateTime.now());
			userDao.updateUser(user);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		return "redirect:/budgets/" + budgetId;
	}
	
	@RequestMapping (value ="/budgets/{budgetId}/editBudget", method = RequestMethod.GET)
	public String getEditBudget(HttpSession session, @PathVariable("budgetId") Long budgetId, Model model) {
		User user = (User) session.getAttribute("user");
		
		Budget budget;
		try {
			budget = budgetDao.getBudgetByBudgetId(budgetId);
			
			Account acc = accountDAO.getAccountByAccountId(budget.getAccountId());
			Set<Account> accounts = accountDAO.getAllAccountsByUserId(user.getUserId());
			BigDecimal amount = budget.getInitialAmount();
			String categoryName = categoryDao.getCategoryNameByCategoryId(budget.getCategoryId());
			Set<String> categories = categoryDao.getAllCategoriesByType(user.getUserId(), "EXPENCE");
			
			Set<Tag> tags = tagDAO.getAllTagsByUserId(user.getUserId());
			LocalDateTime fromDate = budget.getFromDate();
			LocalDateTime toDate = budget.getToDate();
			
			StringBuilder date = new StringBuilder();
			
			date.append(fromDate.getMonthValue()).append("/").append(fromDate.getDayOfMonth()).append("/").append(fromDate.getYear());
			date.append(" - ");
			date.append(toDate.getMonthValue()).append("/").append(toDate.getDayOfMonth()).append("/").append(toDate.getYear());
			
			Set<String> tagNames = new HashSet<String>();
			for (Tag tag : tags) {
				tagNames.add(tag.getName());
			}
			
			session.setAttribute("link", "budgets/" + budgetId + "/editBudget");
			model.addAttribute("categoryName", categoryName);
			model.addAttribute("categories", categories);
			model.addAttribute("tagNames", tagNames);
			model.addAttribute("tags", tags);
			model.addAttribute("editBudgetAmount", amount);
			model.addAttribute("accounts", accounts);
			model.addAttribute("accountName", acc.getName());
			model.addAttribute("budget", budget);
			model.addAttribute("date", date);
			model.addAttribute("newBudget", new Budget());
		} catch (SQLException e) {
			return "error500";
		}
		
		return "editBudget";
	}
	
	@RequestMapping (value ="/budgets/{budgetId}/delete", method = RequestMethod.GET)
	public String deleteBudget(@PathVariable("budgetId") Long budgetId, HttpSession session) {
		User user = (User) session.getAttribute("user");
		
		Budget budget;
		try {
			budget = budgetDao.getBudgetByBudgetId(budgetId);

			budgetDao.deleteBudget(budget);

			user.setLastFill(LocalDateTime.now());
			userDao.updateUser(user);
		} catch (SQLException e) {
			return "error500";
		}
		
		return "redirect:/budgets";
	}
}
