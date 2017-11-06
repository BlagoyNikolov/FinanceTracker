package com.financeTracker.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.financeTracker.model.Transaction;

@Component
public class BudgetsHasTransactionsDAO {
	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private TransactionDAO transactionDAO;
	
	public synchronized void insertTransactionBudget(long budgetId, long transactionId) throws SQLException {
		String sql = "INSERT INTO budgets_has_transactions (budget_id, transaction_id) VALUES (?, ?);";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, budgetId);
		ps.setLong(2, transactionId);
		ps.executeUpdate();
	}
	
	public synchronized void deleteTransactionBudgetByTransactionId(long transactionId) throws SQLException {
		String sql = "DELETE FROM budgets_has_transactions WHERE transaction_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1,  transactionId);
		ps.executeUpdate();
	}
	
	public synchronized void deleteTransactionBudgetByBudgetId(long budgetId) throws SQLException {
		String sql = "DELETE FROM budgets_has_transactions WHERE budget_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1,  budgetId);
		ps.executeUpdate();
	}

	public Set<Transaction> getAllTransactionsByBudgetId(long budgetId) throws SQLException {
		String sql = "SELECT transaction_id FROM budgets_has_transactions WHERE budget_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, budgetId);
		
		ResultSet res = ps.executeQuery();
		
		Set<Transaction> transactions = new HashSet<>();
		
		while(res.next()) {
			transactions.add(transactionDAO.getTransactionByTransactionId(res.getLong("transaction_id")));
		}
		
		return transactions;
	}
}
