package com.financeTracker.model.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.financeTracker.model.Account;
import com.financeTracker.model.Category;
import com.financeTracker.model.PlannedPayment;
import com.financeTracker.model.Tag;
import com.financeTracker.model.TransactionType;

@Component
public class PlannedPaymentDAO {
	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private CategoryDAO categoryDao;
	
	@Autowired
	private TagDAO tagDAO;
	
	public synchronized List<PlannedPayment> getAllPlannedPayments() throws SQLException {
		String query = "SELECT planned_payment_id, name, type, from_date, amount, description, account_id, category_id FROM finance_tracker.planned_payments";
		List<PlannedPayment> payments = new ArrayList<PlannedPayment>();
		
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long plannedPaymentId = result.getLong("planned_payment_id");
			String name = result.getString("name");
			String type = result.getString("type");
			TransactionType paymentType = TransactionType.valueOf(type);
			LocalDateTime fromDate = result.getTimestamp("from_date").toLocalDateTime();
			BigDecimal amount = result.getBigDecimal("amount");
			String description = result.getString("description");
			int accountId = result.getInt("account_id");
			int categoryId = result.getInt("category_id");
			HashSet<Tag> tags = tagDAO.getTagsByPlannedPaymentId(plannedPaymentId);
			String categoryName = categoryDao.getCategoryNameByCategoryId(categoryId);
			PlannedPayment payment = new PlannedPayment(name, paymentType, fromDate, amount, description, accountId, categoryId, tags);
			payment.setPlannedPaymentId(plannedPaymentId);
			payment.setCategoryName(categoryName);
			payments.add(payment);
		}
		
		return payments;
	}
	
	public synchronized List<PlannedPayment> getAllPlannedPaymentsByAccountId(long accountId) throws SQLException {
		String query = "SELECT planned_payment_id, name, type, from_date, amount, description, account_id, category_id FROM finance_tracker.planned_payments WHERE account_id = ?";
		List<PlannedPayment> payments = new ArrayList<PlannedPayment>();
		
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, accountId);
		ResultSet result = statement.executeQuery();
		while(result.next()) {
			long plannedPaymentId = result.getLong("planned_payment_id");
			String name = result.getString("name");
			String type = result.getString("type");
			TransactionType paymentType = TransactionType.valueOf(type);
			LocalDateTime fromDate = result.getTimestamp("from_date").toLocalDateTime();
			BigDecimal amount = result.getBigDecimal("amount");
			String description = result.getString("description");
			int account = result.getInt("account_id");
			int categoryId = result.getInt("category_id");
			HashSet<Tag> tags = tagDAO.getTagsByPlannedPaymentId(plannedPaymentId);
			PlannedPayment payment = new PlannedPayment(name, paymentType, fromDate, amount, description, account, categoryId, tags);
			payment.setPlannedPaymentId(plannedPaymentId);
			payments.add(payment);
		}
		
		return payments;
	}
	
	public synchronized List<PlannedPayment> getAllPlannedPaymentsByCategoryId(long categoryId) throws SQLException {
		List<PlannedPayment> payments = new ArrayList<PlannedPayment>();
		String query = "SELECT planned_payment_id, name, type, from_date, amount, description, account_id, category_id FROM finance_tracker.planned_payments WHERE category_id = ?";
		
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, categoryId);
		ResultSet result = statement.executeQuery();
		while(result.next()) {
			long plannedPaymentId = result.getLong("planned_payment_id");
			String name = result.getString("name");
			String type = result.getString("type");
			TransactionType paymentType = TransactionType.valueOf(type);
			LocalDateTime fromDate = result.getTimestamp("from_date").toLocalDateTime();
			BigDecimal amount = result.getBigDecimal("amount");
			String description = result.getString("description");
			int account = result.getInt("account_id");
			int category = result.getInt("category_id");
			HashSet<Tag> tags = tagDAO.getTagsByPlannedPaymentId(plannedPaymentId);
			PlannedPayment payment = new PlannedPayment(name, paymentType, fromDate, amount, description, account, category, tags);
			payment.setPlannedPaymentId(plannedPaymentId);
			payments.add(payment);
		}
		
		return payments;
	}
	
	public synchronized void insertPlannedPayment(PlannedPayment p) throws SQLException {
		dbManager.getConnection().setAutoCommit(false);
		
		try {
			String query = "INSERT INTO finance_tracker.planned_payments (name, type, from_date, amount, description, account_id, category_id) VALUES (?, ?, STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?, ?)";
			PreparedStatement statement = dbManager.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, p.getName());
			statement.setString(2, p.getPaymentType().toString());
			statement.setTimestamp(3, Timestamp.valueOf(p.getFromDate().withNano(0)));
			statement.setBigDecimal(4, p.getAmount());
			statement.setString(5, p.getDescription());
			statement.setLong(6, p.getAccount());
			statement.setLong(7, p.getCategory());
			statement.executeUpdate();
			
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			p.setPlannedPaymentId(resultSet.getLong(1));
			
			for (Tag tag : p.getTags()) {
				tagDAO.insertTagToTags(tag, tag.getUserId());
				tagDAO.insertTagToPlannedPayment(p, tag);
			}
		} catch (SQLException e) {
			dbManager.getConnection().rollback();
			throw new SQLException();
		} finally {
			dbManager.getConnection().setAutoCommit(true);
		}
	}
	
	public synchronized void updatePlannedPayment(PlannedPayment p) throws SQLException {
		String query = "UPDATE finance_tracker.planned_payments SET name = ?, type = ?, from_date = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), amount = ?, description = ?, account_id = ?, category_id = ? WHERE planned_payment_id = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setString(1, p.getName());
		statement.setString(2, p.getPaymentType().toString());
		statement.setTimestamp(3, Timestamp.valueOf(p.getFromDate().withNano(0)));
		statement.setBigDecimal(4, p.getAmount());
		statement.setString(5, p.getDescription());
		statement.setLong(6, p.getAccount());
		statement.setLong(7, p.getCategory());
		statement.setLong(8, p.getPlannedPaymentId());
		statement.executeUpdate();
		
		for (Tag tag : p.getTags()) {
			tagDAO.insertTagToTags(tag, tag.getUserId());
			tagDAO.insertTagToPlannedPayment(p, tag);
		}
	}
	
	public synchronized void deletePlannedPayment(long plannedPaymentId) throws SQLException {
		dbManager.getConnection().setAutoCommit(false);
		
		try {
			tagDAO.deleteAllTagsForPlannedPayment(plannedPaymentId);
			
			String query = "DELETE FROM finance_tracker.planned_payments WHERE planned_payment_id = ?";
		
			PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
			statement.setLong(1, plannedPaymentId);
			statement.executeUpdate();
		} catch (Exception e) {
			dbManager.getConnection().rollback();
			
			throw new SQLException();
		} finally {
			dbManager.getConnection().setAutoCommit(true);
		}
	}

	public PlannedPayment getPlannedPaymentByPlannedPaymentId(Long plannedPaymentId) throws SQLException {
		String query = "SELECT planned_payment_id, name, type, from_date, amount, description, account_id, category_id FROM finance_tracker.planned_payments WHERE planned_payment_id = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, plannedPaymentId);
		statement.executeQuery();
		
		ResultSet result = statement.executeQuery();
		result.next();
		
		String name = result.getString("name");
		TransactionType plannedPaymentType = TransactionType.valueOf(result.getString("type"));
		LocalDateTime fromDate = result.getTimestamp("from_date").toLocalDateTime();
		BigDecimal amount = result.getBigDecimal("amount");
		String description = result.getString("description");
		int accountId = result.getInt("account_id");
		int categoryId = result.getInt("category_id");
		HashSet<Tag> tags = tagDAO.getTagsByPlannedPaymentId(plannedPaymentId);
		
		PlannedPayment p = new PlannedPayment(name, plannedPaymentType, fromDate, amount, description, accountId, categoryId, tags);
		p.setPlannedPaymentId(plannedPaymentId);
		
		return p;
	}
	
	public List<PlannedPayment> getAllPlannedPaymentsByUserId(long userId) throws SQLException {
		String query = "SELECT planned_payment_id, p.name, p.type, p.from_date, p.amount, p.description, p.account_id, p.category_id "
				+ "FROM finance_tracker.planned_payments p "
				+ "JOIN finance_tracker.accounts a ON p.account_id = a.account_id AND user_id = ?;";
		List<PlannedPayment> payments = new ArrayList<PlannedPayment>();
		
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, userId);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long plannedPaymentId = result.getLong("planned_payment_id");
			String name = result.getString("name");
			String type = result.getString("type");
			TransactionType paymentType = TransactionType.valueOf(type);
			LocalDateTime fromDate = result.getTimestamp("from_date").toLocalDateTime();
			BigDecimal amount = result.getBigDecimal("amount");
			String description = result.getString("description");
			int accountId = result.getInt("account_id");
			int categoryId = result.getInt("category_id");
			HashSet<Tag> tags = tagDAO.getTagsByPlannedPaymentId(plannedPaymentId);
			String categoryName = categoryDao.getCategoryNameByCategoryId(categoryId);
			PlannedPayment payment = new PlannedPayment(name, paymentType, fromDate, amount, description, accountId, categoryId, tags);
			payment.setPlannedPaymentId(plannedPaymentId);
			payment.setCategoryName(categoryName);
			payments.add(payment);
		}
		return payments;
	}
}
