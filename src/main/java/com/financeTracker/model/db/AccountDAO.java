package com.financeTracker.model.db;

import java.math.BigDecimal;
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

import com.financeTracker.model.Account;
import com.financeTracker.model.Budget;
import com.financeTracker.model.PlannedPayment;
import com.financeTracker.model.Transaction;
import com.financeTracker.model.User;

@Component
public class AccountDAO {
	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private BudgetDAO budgetDao;
	
	@Autowired
	private PlannedPaymentDAO plannedPaymentDAO;
	
	@Autowired
	private TransactionDAO transactionDAO;
	
	public synchronized void insertAccount(Account acc) throws SQLException {
		Connection con = dbManager.getConnection();
		PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, amount, user_id) "
														+ "VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		ps.setString(1, acc.getName());
		ps.setBigDecimal(2, acc.getAmount());
		ps.setInt(3, (int)acc.getUserId());
		ps.executeUpdate();
		
		ResultSet res = ps.getGeneratedKeys();
		res.next();
		
		acc.setAccaountId(res.getLong(1));
	}
	
	public synchronized void deleteAccount(long accountId) throws SQLException {
		dbManager.getConnection().setAutoCommit(false);
		
		try {
			Account acc = getAccountByAccountId(accountId);
			
			for (Budget budget : acc.getBudgets()) {
				budgetDao.deleteBudget(budget);
			}
			
			for (Transaction transaction : acc.getTransactions()) {
				transactionDAO.deleteTransaction(transaction);
			}
			
			for (PlannedPayment payment : acc.getPlannedPayments()) {
				plannedPaymentDAO.deletePlannedPayment(payment.getPlannedPaymentId());;
			}
			
			String sql = "DELETE FROM accounts WHERE account_id = ?;";
			
			PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
			ps.setLong(1, accountId);
			ps.executeUpdate();
		} catch (Exception e) {
			dbManager.getConnection().rollback();
			
			throw new SQLException();
		} finally {
			dbManager.getConnection().setAutoCommit(true);
		}
	}
	
	public synchronized long getAccountId(User user, String name) throws SQLException {
		String sql = "SELECT account_id FROM accounts WHERE user_id = ? and name = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, user.getUserId());
		ps.setString(2, name);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		return res.getLong("account_id");
	}
	
	public synchronized Set<Account> getAllAccountsByUserId(long userId) throws SQLException {
		Set<Account> accounts = new HashSet<>();
		
		String sql = "SELECT account_id, name, amount FROM accounts WHERE user_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, userId);
		
		ResultSet res = ps.executeQuery();
		
		while(res.next()) {
			long accountId = res.getInt("account_id");
			String name = res.getString("name");
			BigDecimal amount = new BigDecimal(res.getDouble("amount"));
			List<Transaction> transactions = transactionDAO.getAllTransactionsByAccountId(accountId);
			List<Budget> budgets = budgetDao.getAllBudgetsByAccountId(accountId);
			List<PlannedPayment> plannedPayments = plannedPaymentDAO.getAllPlannedPaymentsByAccountId(accountId);
			
			Account acc = new Account(name, amount, userId, transactions, budgets, plannedPayments);
			acc.setAccaountId(accountId);
			
			accounts.add(acc);

		}
		
		return accounts;
	}
	
	public synchronized Account getAccountByAccountId(long accountId) throws SQLException {
		String sql = "SELECT name, amount, user_id FROM accounts WHERE accounts.account_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, accountId);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		String name = res.getString("name");
		BigDecimal amount = res.getBigDecimal("amount");
		int userId = res.getInt("user_id");
		List<Transaction> transactions = transactionDAO.getAllTransactionsByAccountId(accountId);
		List<Budget> budgets = budgetDao.getAllBudgetsByAccountId(accountId);
		List<PlannedPayment> plannedPayments = plannedPaymentDAO.getAllPlannedPaymentsByAccountId(accountId);
		
		Account acc = new Account(name, amount, userId, transactions, budgets, plannedPayments);
		acc.setAccaountId(accountId);
		
		return acc;
	}
	
	public synchronized void updateAccountAmount(Account acc, BigDecimal newAmount) throws SQLException {
		String sql = "UPDATE accounts SET amount = ? WHERE account_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setBigDecimal(1, newAmount);
		ps.setLong(2, acc.getAccountId());
		ps.executeUpdate();
	}
	
	public synchronized void makeTransferToOtherAccount(Account currentAcc, Account otherAcc, BigDecimal amount, Transaction from, Transaction to) throws SQLException {
		dbManager.getConnection().setAutoCommit(false);
		
		try {
			updateAccountAmount(currentAcc, currentAcc.getAmount().subtract(amount));
			updateAccountAmount(otherAcc, otherAcc.getAmount().add(amount));
			transactionDAO.insertTransaction(from);
			transactionDAO.insertTransaction(to);
			dbManager.getConnection().commit();
		} catch (Exception e) {
			dbManager.getConnection().rollback();
		} finally {
			dbManager.getConnection().setAutoCommit(true);
		}
	}
	
	public synchronized BigDecimal getAmountByAccountId(long accountId) throws SQLException {
		String sql = "SELECT amount FROM accounts WHERE account_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, accountId);

		ResultSet res = ps.executeQuery();
		res.next();
		
		return res.getBigDecimal("amount");
	}
	
	public synchronized boolean isValidAccount(User user, String name) throws SQLException {
		Set<Account> accounts = getAllAccountsByUserId((int)user.getUserId());
		
		for (Account acc : accounts) {
			if (acc.getName().equals(name)) {
				return false;
			}
		}
		
		return true;
	}
	
	public String getAccountNameByAccountId(long accountId) throws SQLException {
		String query = "SELECT name FROM accounts WHERE accounts.account_id = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, accountId);
		
		ResultSet res = statement.executeQuery();
		res.next();
		return res.getString("name");
	}
	
	public Account getAccountByUserIDAndAccountName(long userId, String name) throws SQLException {
		String sql = "SELECT account_id, amount FROM accounts WHERE user_id = ? AND name = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, userId);
		ps.setString(2, name);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		long accountId = res.getLong("account_id");
		BigDecimal amount = res.getBigDecimal("amount");
		
		Account acc = new Account(name, amount, userId);
		acc.setAccaountId(accountId);
		
		return acc;
	}
	
	public Account getAccountByAccountNameAndAccountId(String accountName, long userId) throws SQLException {
		String query = "SELECT account_id, name, amount, user_id FROM finance_tracker.accounts WHERE accounts.name = ? AND user_id = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setString(1, accountName);
		statement.setLong(2, userId);
		
		ResultSet res = statement.executeQuery();
		res.next();
		
		long accountId = res.getLong("account_id");
		String name = res.getString("name");
		BigDecimal amount = res.getBigDecimal("amount");
		List<Transaction> transactions = transactionDAO.getAllTransactionsByAccountId(accountId);
		List<Budget> budgets = budgetDao.getAllBudgetsByAccountId(accountId);
		List<PlannedPayment> plannedPayments = plannedPaymentDAO.getAllPlannedPaymentsByAccountId(accountId);
		
		Account account = new Account(name, amount, userId, transactions, budgets, plannedPayments);
		account.setAccaountId(accountId);
		
		return account;
	}
}
