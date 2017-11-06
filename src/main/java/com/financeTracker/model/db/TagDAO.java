package com.financeTracker.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.financeTracker.model.Budget;
import com.financeTracker.model.PlannedPayment;
import com.financeTracker.model.Tag;
import com.financeTracker.model.Transaction;

@Component
public class TagDAO {
	@Autowired
	private DBManager dbManager;
	
	public Tag getTagByTagId(long tagId) throws SQLException {
		String query = "SELECT name, user_id FROM finance_tracker.tags WHERE finance_tracker.tags.tag_id = ?";
		Tag tag = null;
		PreparedStatement statement = null;
		statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, tagId);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			String name = result.getString("name");
			long userId = result.getLong("user_id");
			tag = new Tag(tagId, name, userId);
		}
		return tag;
	}
	
	public synchronized void insertTagToTags(Tag tag, long userId) throws SQLException {
		String query = "INSERT INTO finance_tracker.tags (name, user_id) VALUES (?, ?)";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, tag.getName());
		statement.setLong(2, userId);
		statement.executeUpdate();
		
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		tag.setTagId(resultSet.getLong(1));
	}
	
	public HashSet<Tag> getTagsByTransactionId(long transactionId) throws SQLException {
		HashSet<Tag> tags = new HashSet<Tag>();
		String query = "SELECT tag_id, transaction_id FROM finance_tracker.transactions_has_tags WHERE transaction_id = ?";
		
		PreparedStatement statement = null;
		statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, transactionId);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long tagId = result.getLong("tag_id");
			Tag tag = getTagByTagId(tagId);
			tags.add(tag);
		}
		return tags;
	}
	
	public void insertTagToTransaction(Transaction t, Tag tag) throws SQLException {
		String query = "INSERT INTO finance_tracker.transactions_has_tags (transaction_id, tag_id) VALUES (?, ?)";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, t.getTransactionId());
		statement.setLong(2, tag.getTagId());
		statement.executeUpdate();
	}

	public HashSet<Tag> getTagsByBudgetId(long budgetId) throws SQLException {
		HashSet<Tag> tags = new HashSet<Tag>();
		String query = "SELECT budget_id, tag_id FROM finance_tracker.budgets_has_tags WHERE budget_id = ?";
		
		PreparedStatement statement = null;
		statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, budgetId);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long tagId = result.getLong("tag_id");
			Tag tag = getTagByTagId(tagId);
			tags.add(tag);
		}
		return tags;
	}
	
	public synchronized void insertTagToBudget(Budget b, Tag tag) throws SQLException {
		String query = "INSERT INTO finance_tracker.budgets_has_tags (budget_id, tag_id) VALUES (?, ?)";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, b.getBudgetId());
		statement.setLong(2, tag.getTagId());
		statement.executeUpdate();
	}

	public HashSet<Tag> getTagsByPlannedPaymentId(long plannedPaymentId) throws SQLException {
		HashSet<Tag> tags = new HashSet<Tag>();
		String query = "SELECT planned_payment_id, tag_id FROM finance_tracker.planned_payments_has_tags WHERE planned_payment_id = ?";
		
		PreparedStatement statement = null;
		statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, plannedPaymentId);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long tagId = result.getLong("tag_id");
			Tag tag = getTagByTagId(tagId);
			tags.add(tag);
		}
		return tags;
	}
	
	public void insertTagToPlannedPayment(PlannedPayment p, Tag tag) throws SQLException {
		String query = "INSERT INTO finance_tracker.planned_payments_has_tags (planned_payment_id, tag_id) VALUES (?, ?)";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, p.getPlannedPaymentId());
		statement.setLong(2, tag.getTagId());
		statement.executeUpdate();
		
	}

	public Set<Tag> getAllTagsByUserId(long userId) throws SQLException {
		String query = "SELECT tag_id, name FROM tags WHERE user_id = ?;";
		
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, userId);
		
		ResultSet result = statement.executeQuery();
		
		Set<Tag> tags = new HashSet<>();
		
		while(result.next()) {
			long tagId = result.getLong("tag_id");
			Tag tag = getTagByTagId(tagId);
			
			tags.add(tag);
		}
		
		return tags;
	}
	
	public synchronized void deleteAllTagsForBydget(long budgetId) throws SQLException {
		String sql = "DELETE FROM budgets_has_tags WHERE budget_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, budgetId);
		
		ps.executeUpdate();
	}

	public void deleteAllTagsForTransaction(long transactionId) throws SQLException {
		String query = "DELETE FROM finance_tracker.transactions_has_tags WHERE transaction_id = ?;";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, transactionId);
		statement.executeUpdate();
	}

	public void deleteAllTagsForPlannedPayment(long plannedPaymentId) throws SQLException {
		String query = "DELETE FROM finance_tracker.planned_payments_has_tags WHERE planned_payment_id = ?;";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, plannedPaymentId);
		statement.executeUpdate();
	}

	public boolean tagExists(long tagId, long userId) {
		String query = "SELECT tag_id FROM tags WHERE tag_id = ? AND user_id = ?";
		return false;
	}
	
	public Tag getTagByNameAndUser(String name, long userId) throws SQLException {
		String sql = "SELECT tag_id FROM tags WHERE name = ? AND user_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, name);
		ps.setLong(2, userId);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		Tag t = new Tag(res.getLong("tag_id"), name, userId);
		
		return t;
	}
}
