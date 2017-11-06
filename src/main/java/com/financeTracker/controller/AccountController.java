package com.financeTracker.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.financeTracker.model.Account;
import com.financeTracker.model.User;
import com.financeTracker.model.db.AccountDAO;
import com.financeTracker.model.db.UserDAO;

@Controller
public class AccountController {
	
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private UserDAO userDao;

	@RequestMapping(value="/addAccount", method=RequestMethod.POST)
	public String addAccount(HttpServletRequest request, HttpSession session, Model viewModel, @Valid @ModelAttribute("account") Account account, BindingResult bindingResult) {
		
		if (bindingResult.hasErrors()) {
			viewModel.addAttribute("error", "Could not create account. Please, enter a valid name and amount!");
			
			return "addAccount";
	 	}
		
		User user = (User) session.getAttribute("user");
		try {
			account.setUserID(user.getUserId());
			accountDAO.insertAccount(account);
			
			user.addAccount(account);
			
			user.setLastFill(LocalDateTime.now());
			userDao.updateUser(user);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		return "redirect:main";
	}
	
	@RequestMapping(value="/addAccount", method=RequestMethod.GET)
	public String makeAccount(HttpServletRequest request, HttpSession session, Model viewModel) {
		Account acc = new Account();
		
		viewModel.addAttribute("account", acc);
		
		return "addAccount";
	}
	
	@RequestMapping(value="/account/deleteAccount/{accountId}", method=RequestMethod.POST)
	public String deleteAccount(@Valid @PathVariable("accountId") Long accountId, HttpSession session) {
		User user = (User) session.getAttribute("user");
		
		try {
			user.removeAccount(accountDAO.getAccountByAccountId(accountId));

			accountDAO.deleteAccount(accountId);
			
			user.setLastFill(LocalDateTime.now());
			userDao.updateUser(user);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		return "redirect:/main";
	}
}
