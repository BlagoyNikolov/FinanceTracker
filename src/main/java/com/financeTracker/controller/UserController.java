package com.financeTracker.controller;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.financeTracker.model.Account;
import com.financeTracker.model.User;
import com.financeTracker.model.db.AccountDAO;
import com.financeTracker.model.db.TransactionDAO;
import com.financeTracker.model.db.UserDAO;
import com.financeTracker.threads.EmailService;
import com.financeTracker.threads.PlannedPaymentService;
import com.financeTracker.util.EmailSender;

@Controller
public class UserController {
	
	@Autowired
	private PlannedPaymentService paymentService;


	@Autowired
	public UserController(EmailService emailService) {
		System.out.println("Services started!");
		Thread t = new Thread(emailService);
		t.setDaemon(true);
		t.start();
	}
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private TransactionDAO transactionDAO;
	
	@RequestMapping(value="/index", method=RequestMethod.GET)
	public String welcome(HttpSession session, Model viewModel) {
		if(session.getAttribute("user") == null){
			User user = new User();
			viewModel.addAttribute("user", user);
			return "login";
		}
		else{
			return "redirect:main";
		}
	}
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String login(HttpServletRequest request, HttpSession session, Model viewModel, @ModelAttribute("user") User user) {
		try {
			if (userDAO.isValidLogin(user.getUsername(), user.getPassword())) {
				User u = userDAO.getUser(user.getUsername());
				session.setAttribute("user", u);
				return "redirect:main";
			}
			else{
				session.setAttribute("logged", false);
				viewModel.addAttribute("login", "Could not login. Please, enter a valid username and password!");
				User u = new User();
				viewModel.addAttribute("user", u);
				return "login";
			}
		} catch (SQLException e) {
			System.out.println("DB error");
			return "error500";
		}
	}
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public String register(HttpServletRequest request, HttpSession session, Model viewModel, @Valid @ModelAttribute("user") User user, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			viewModel.addAttribute("register", "Could not create profile. Please, enter a valid username and password!");
			
			User u = new User();
			
			viewModel.addAttribute("user", u);
			
            return "register";
	 	}
		
		String repeatPassword = request.getParameter("repeatPassword");
		
		if(!MessageDigest.isEqual(DigestUtils.sha512(repeatPassword), user.getPassword())){
			request.setAttribute("error", "passwords missmatch");
			viewModel.addAttribute("register", "passwords missmatch");
			
			return "register";
		}
		
		try {
			if (!userDAO.existsUser(user.getUsername())) {
				
				user.setPasswordToken(DigestUtils.sha512Hex(DigestUtils.sha512Hex(user.getPassword()) + user.getPassword()));
				
				userDAO.insertUser(user);
				
				session.setAttribute("user", user);
				
				EmailSender.sendSimpleEmail(user.getEmail(), "Welcome to the Finance Tracker", "Your new profile is ready. Track away!");
				
				return "main";
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		return "login";
	}
	
	@RequestMapping(value="/main", method=RequestMethod.GET)
	public String main(HttpSession session, Model viewModel) {
		User u =  (User) session.getAttribute("user");
		
		TreeSet<Account> accounts = new TreeSet<>((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
		try {
			accounts.addAll(accountDAO.getAllAccountsByUserId(u.getUserId()));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		viewModel.addAttribute("accounts", accounts);
		
		BigDecimal allBalance = BigDecimal.valueOf(0);
		for (Account account : accounts) {
			allBalance = allBalance.add(account.getAmount());
		}
		String balance = NumberFormat.getCurrencyInstance().format(allBalance);
		
		try {
			Map<LocalDate, BigDecimal> defaultTransactions = transactionDAO.getTransactionAmountAndDate(u.getUserId(), 0);
			
			Map<LocalDate, BigDecimal> reverseDefaultTransactions = new TreeMap<LocalDate,BigDecimal>(Collections.reverseOrder());
			reverseDefaultTransactions.putAll(defaultTransactions);
			
			for (LocalDate date : reverseDefaultTransactions.keySet()) {
				BigDecimal transactionAmount = reverseDefaultTransactions.get(date);
				allBalance = allBalance.subtract(transactionAmount);
				reverseDefaultTransactions.put(date, transactionAmount.add(allBalance));
			}
			
			Map<LocalDate, BigDecimal> finalDefaultTransactions = new TreeMap<LocalDate,BigDecimal>();
			finalDefaultTransactions.putAll(reverseDefaultTransactions);
			
			viewModel.addAttribute("defaultTransactions", finalDefaultTransactions);
		} catch (SQLException e) {
			return "error500";
		}
		
		viewModel.addAttribute("balance", balance);
		return "main";
	}
	
	@RequestMapping(value="/login", method=RequestMethod.GET)
	public String getLogin(Model viewModel) {
		User user = new User();
		
		viewModel.addAttribute("user", user);
		
		return "login";
	}
	
	@RequestMapping(value="/register", method=RequestMethod.GET)
	public String getRegister(Model m) {
		User user = new User();
		
		m.addAttribute("user", user);
		
		return "register";
	}
	
	@RequestMapping(value="/about", method=RequestMethod.GET)
	public String about() {
		return "about";
	}
	
	@RequestMapping(value="/logout", method=RequestMethod.GET)
	public String logout(HttpSession session) {
		session.invalidate();
		return "forward:index";
	}
	
	@RequestMapping(value="/user", method=RequestMethod.GET)
	public String user(HttpSession session, Model model) {
		User u =  (User) session.getAttribute("user");
		
		String username = u.getUsername();
		String email = u.getEmail();
		String firstName = u.getFirstName();
		String lastName = u.getLastName();
		
		model.addAttribute("username", username);
		model.addAttribute("email", email);
		model.addAttribute("firstName", firstName);
		model.addAttribute("lastName", lastName);
		model.addAttribute("user", new User());
		session.setAttribute("userId", u.getUserId());
		return "user";
	}
	
	@RequestMapping(value="/user/edit", method=RequestMethod.POST)
	public String UpdateUser(HttpSession session, HttpServletRequest request, Model model, @Valid @ModelAttribute("user") User newUser, BindingResult bindingResult) {
		User user = (User) session.getAttribute("user");
		
		
		
		if (!MessageDigest.isEqual(DigestUtils.sha512(DigestUtils.sha512Hex(newUser.getPassword())), user.getPassword())) {
			model.addAttribute("editUser", "Could not edit your profile. Please, enter a correct password!");
			String username = user.getUsername();
			String email = user.getEmail();
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			
			model.addAttribute("username", username);
			model.addAttribute("email", email);
			model.addAttribute("firstName", firstName);
			model.addAttribute("lastName", lastName);
			model.addAttribute("user", new User());
			session.setAttribute("userId", user.getUserId());
			
			return "user";
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("editUser", "Could not edit your profile. Please, enter a valid email, first name and last name!");
			String username = user.getUsername();
			String email = user.getEmail();
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			
			model.addAttribute("username", username);
			model.addAttribute("email", email);
			model.addAttribute("firstName", firstName);
			model.addAttribute("lastName", lastName);
			model.addAttribute("user", new User());
			session.setAttribute("userId", user.getUserId());
			
			return "user";
		}
		
		
		user.setEmail(newUser.getEmail());
		user.setFirstName(newUser.getFirstName());
		user.setLastName(newUser.getLastName());
		
		try {
			userDAO.updateUser(user);
			
			session.setAttribute("user", user);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		return "redirect:/main";
	}
	
	@RequestMapping(value = "/forgottenPassword", method = RequestMethod.GET)
	public String forgottenPassword() {
		return "forgottenPassword";
	}
	
	@RequestMapping(value = "/forgottenPassword", method = RequestMethod.POST)
	public String sendEmail(HttpServletRequest request, Model model, HttpSession session) {
		String email = request.getParameter("email");
		
		try {
			if (userDAO.existEmail(email)) {
				User user = userDAO.getUserByEmail(email);
				
				EmailSender.sendSimpleEmail(email, "FT FORGOTTEN PASSWORD", "Hello " + user.getFirstName() + " " + user.getLastName() + ",\n" 
				+ "Click on the link to change password:\n" + "http://localhost:8080/FinanceTracker/resetPassword/" + user.getPasswordToken());
			} else {
				model.addAttribute("forgottenPassword", "Invalid email :(");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		return "forgottenPassword";
	}
	
	@RequestMapping(value = "/resetPassword/{passwordToken}", method = RequestMethod.GET)
	public String resetPassword(Model model, @PathVariable("passwordToken") String passwordToken) {
		model.addAttribute("token", passwordToken);
		
		return "resetPassword";
	}
	
	@RequestMapping(value = "/resetPassword/{passwordToken}", method = RequestMethod.POST)
	public String getNewPassword(HttpServletRequest request, Model model, @PathVariable("passwordToken") String passwordToken) {
		String password = request.getParameter("password");
		String repeatPassword = request.getParameter("repeatPassword");
		
		try {
			if (userDAO.existUserByPasswordToken(passwordToken) && password.equals(repeatPassword)) {
				User user = userDAO.getUserByPasswordToken(passwordToken);
				
				userDAO.updateUserPassword(user, password);
			} else {
				model.addAttribute("ressetPassword", "Enter valid date");
				
				return "resetPassword";
			}
			
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			return "error500";
		}
		
		User user = new User();
		
		model.addAttribute("user", user);
		
		return "redirect:/login";
	}
}
