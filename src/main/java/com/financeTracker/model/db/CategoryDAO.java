package com.financeTracker.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.financeTracker.model.Budget;
import com.financeTracker.model.Category;
import com.financeTracker.model.PlannedPayment;
import com.financeTracker.model.Transaction;
import com.financeTracker.model.TransactionType;
import com.financeTracker.model.User;

@Component
public class CategoryDAO {
	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private BudgetDAO budgetDao;
	
	@Autowired
	private PlannedPaymentDAO plannedPaymentDAO;
	
	@Autowired
	private TransactionDAO transactionDAO;
	
	public synchronized void insertCategory(Category c) throws SQLException {
		Connection con = dbManager.getConnection();
		PreparedStatement ps = con.prepareStatement("INSERT INTO categories (name, type, user_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		ps.setString(1, c.getName());
		ps.setString(2, c.getType().toString());
		ps.setLong(3, c.getUserId());
		ps.executeUpdate();
		
		ResultSet res = ps.getGeneratedKeys();
		res.next();
		
		c.setCategoryID(res.getLong(1));
	}
	
	public synchronized String getCategoryNameByCategoryId(long categoryId) throws SQLException {
		String query = "SELECT name FROM finance_tracker.categories WHERE category_id = ?";
		PreparedStatement ps = dbManager.getConnection().prepareStatement(query);
		ps.setLong(1, categoryId);
		
		ResultSet res = ps.executeQuery();
		res.next();
		String name = res.getString("name");
		return name;
	}
	
	public synchronized Category getCategoryByCategoryId(long categoryId) throws SQLException {
		String sql = "SELECT name, type, user_id FROM categories WHERE category_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, categoryId);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		String name = res.getString("name");
		TransactionType type = TransactionType.valueOf(res.getString("type"));
		Long userId = new Long(res.getLong("user_id"));
		List<Transaction> transactions = transactionDAO.getAllTransactionsByCategoryId(categoryId);
		List<Budget> budgets = budgetDao.getAllBudgetsByCategoryId(categoryId);
		List<PlannedPayment> plannedPayments = plannedPaymentDAO.getAllPlannedPaymentsByCategoryId(categoryId);
		
		Category category = new Category(name, type, userId, transactions, budgets, plannedPayments);
		
		return category;
	}
	
	public synchronized Set<Category> getAllCategoriesByUserId(Long ... userIdParams) throws SQLException {
		Long userId = null;
		for (long l : userIdParams) {
			userId = l;
		}
		String sql = "";
		PreparedStatement ps = null;
		if (userId == null) {
			sql = "SELECT category_id, name, type FROM categories WHERE user_id IS NULL;";
			ps = dbManager.getConnection().prepareStatement(sql);
		} else {
			sql = "SELECT category_id, name, type FROM categories WHERE user_id = ?;";
			ps = dbManager.getConnection().prepareStatement(sql);
			ps.setLong(1, userId);
		}
		
		Set<Category> categories = new HashSet<>();
		ResultSet res = ps.executeQuery();
		
		while(res.next()) {
			long categoryId = res.getLong("category_id");
			String name = res.getString("name");
			TransactionType type = TransactionType.valueOf(res.getString("type"));
			List<Transaction> transactions = transactionDAO.getAllTransactionsByCategoryId(categoryId);
			List<Budget> budgets = budgetDao.getAllBudgetsByCategoryId(categoryId);
			List<PlannedPayment> plannedPayments = plannedPaymentDAO.getAllPlannedPaymentsByCategoryId(categoryId);
			
			categories.add(new Category(name, type, userId, transactions, budgets, plannedPayments));
		}
		
		return categories;
	}
	
	public synchronized boolean isValidOwnCategory(User user, String name) throws SQLException {
		Set<Category> ownCategories = getAllCategoriesByUserId(user.getUserId());
		
		for (Category ownCategory : ownCategories) {
			if (ownCategory.getName().equals(name)) {
				return false;
			}
		}
		
		return true;
	}

	public Category getCategoryByCategoryName(String categoryName) throws SQLException {
		String query = "SELECT category_id, name, type, user_id FROM finance_tracker.categories WHERE name = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setString(1, categoryName);
		
		ResultSet resultSet = statement.executeQuery();
		resultSet.next();
		
		long categoryId = resultSet.getLong("category_id");
		String name = resultSet.getString("name");
		TransactionType type = TransactionType.valueOf(resultSet.getString("type"));
		Long userId = new Long(resultSet.getLong("user_id"));
		List<Transaction> transactions = transactionDAO.getAllTransactionsByCategoryId(categoryId);
		List<Budget> budgets = budgetDao.getAllBudgetsByCategoryId(categoryId);
		List<PlannedPayment> plannedPayments = plannedPaymentDAO.getAllPlannedPaymentsByCategoryId(categoryId);
		
		Category category = new Category(name, type, userId, transactions, budgets, plannedPayments);
		category.setCategoryID(categoryId);
		
		return category;
	}
	
	public synchronized Set<String> getAllCategoriesByType(long userId, String type) throws SQLException {
		Set<String> categoriesNames = new HashSet<String>();
		String query = "SELECT name, user_id, type FROM finance_tracker.categories WHERE (user_id = ? OR user_id IS NULL) AND type = ?";
		PreparedStatement ps = dbManager.getConnection().prepareStatement(query);
		ps.setLong(1, userId);
		ps.setString(2, type);
		
		ResultSet resultSet = ps.executeQuery();
		while(resultSet.next()) {
			String name = resultSet.getString("name");
			categoriesNames.add(name);
		}
		
		return categoriesNames;
	}
}
