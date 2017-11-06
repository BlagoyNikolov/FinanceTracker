package com.financeTracker.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.financeTracker.model.Budget;
import com.financeTracker.model.Category;
import com.financeTracker.model.Transaction;
import com.financeTracker.model.User;
import com.financeTracker.model.db.CategoryDAO;
import com.financeTracker.model.db.UserDAO;

@Controller
public class CategoryController {
	
	@Autowired
	private CategoryDAO categoryDAO;
	
	@Autowired
	private UserDAO userDao;
	
	@RequestMapping(value="/addCategory", method=RequestMethod.GET)
	public String displayCategory(HttpSession session, Model model, HttpServletRequest request) {
		String link = (String) session.getAttribute("link");
		
		Category category = new Category();
		
		model.addAttribute("link", link);
		model.addAttribute("category", category);
		return "category";
	}
	
	@RequestMapping(value="/addCategory", method=RequestMethod.POST)
	public String addCategory(HttpServletRequest request, HttpSession session, Model viewModel, @Valid @ModelAttribute("category") Category category, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			viewModel.addAttribute("error", "Could not create category. Please, enter a valid name and type!");
			
			return "category";
	 	}
		
		String link = (String) session.getAttribute("link");
		
		User user = (User) request.getSession().getAttribute("user");
		try {
			category.setUserId(user.getUserId());
			categoryDAO.insertCategory(category);
			
			user.setLastFill(LocalDateTime.now());
			userDao.updateUser(user);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		return "redirect:" + link;
	}
}
