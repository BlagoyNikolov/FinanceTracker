package com.financeTracker.model.db;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.financeTracker.model.Account;
import com.financeTracker.model.Budget;
import com.financeTracker.model.Tag;
import com.financeTracker.model.Transaction;
import com.financeTracker.model.TransactionType;

@Component
public class TransactionDAO {
	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private BudgetDAO budgetDao;
	
	@Autowired
	private BudgetsHasTransactionsDAO budgetsHasTransactionsDAO;
	
	@Autowired
	private CategoryDAO categoryDao;
	
	@Autowired
	private TagDAO tagDAO;
	
	@Autowired
	private AccountDAO accountDAO;
	
	private static final HashMap<TransactionType, ArrayList<Transaction>> ALL_TRANSACTIONS = new HashMap<>();

	@PostConstruct
	private void init() throws SQLException {
		ALL_TRANSACTIONS.put(TransactionType.EXPENCE, new ArrayList<>());
		ALL_TRANSACTIONS.put(TransactionType.INCOME, new ArrayList<>());
		getAllTransactions();
	}
	
	public synchronized void getAllTransactions() throws SQLException {
		String query = "SELECT transaction_id, type, date, description, amount, account_id, category_id FROM finance_tracker.transactions";
		PreparedStatement statement = null;
		statement = dbManager.getConnection().prepareStatement(query);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long transactionId = result.getInt("transaction_id");
			String type = result.getString("type");
			TransactionType transactionType = TransactionType.valueOf(type);
			LocalDateTime date = result.getTimestamp("date").toLocalDateTime();
			BigDecimal amount = result.getBigDecimal("amount");
			String description = result.getString("description");
			int accountId = result.getInt("account_id");
			int categoryId = result.getInt("category_id");
			HashSet<Tag> tags = tagDAO.getTagsByTransactionId(transactionId);
			String categoryName = categoryDao.getCategoryNameByCategoryId(categoryId);
			Transaction t = new Transaction(transactionId, transactionType, description, amount, accountId, categoryId, date, tags);
			t.setCategoryName(categoryName);
			ALL_TRANSACTIONS.get(t.getType()).add(t);
		}
	}

	public synchronized List<Transaction> getAllTransactionsByAccountId(long accountId) throws SQLException {
		List<Transaction> transactions = new ArrayList<Transaction>();
		String query = "SELECT transaction_id, type, date, description, amount, account_id, category_id FROM finance_tracker.transactions WHERE account_id = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, accountId);
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long transactionId = result.getInt("transaction_id");
			String type = result.getString("type");
			TransactionType transactionType = TransactionType.valueOf(type);
			LocalDateTime date = result.getTimestamp("date").toLocalDateTime();
			BigDecimal amount = result.getBigDecimal("amount");
			String description = result.getString("description");
			int account = result.getInt("account_id");
			int categoryId = result.getInt("category_id");
			HashSet<Tag> tags = tagDAO.getTagsByTransactionId(transactionId);
			String categoryName = categoryDao.getCategoryNameByCategoryId(categoryId);
			Transaction t = new Transaction(transactionId, transactionType, description, amount, account, categoryId, date, tags);
			t.setCategoryName(categoryName);
			transactions.add(t);
		}
		
		return transactions;
	}
	
	public synchronized List<Transaction> getAllTransactionsByCategoryId(long categoryId) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (ArrayList<Transaction> transactionTypes : ALL_TRANSACTIONS.values()) {
			for (Transaction transaction : transactionTypes) {
				if (transaction.getCategory() == categoryId) {
					transactions.add(transaction);
				}
			}
		}
		return transactions;
	}
	
	
	public synchronized Transaction getTransactionByTransactionId(long transactionId) throws SQLException {
		String sql = "SELECT type, date, description, amount, account_id, category_id FROM transactions WHERE transaction_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, transactionId);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		TransactionType transactionType = TransactionType.valueOf(res.getString("type"));
		LocalDateTime date = res.getTimestamp("date").toLocalDateTime();
		String description = res.getString("description");
		BigDecimal amount = res.getBigDecimal("amount");
		int accountId = res.getInt("account_id");
		int categoryId = res.getInt("category_id");
		HashSet<Tag> tags = tagDAO.getTagsByTransactionId(transactionId);
		
		Transaction t = new Transaction(transactionId, transactionType, description, amount, accountId, categoryId, date, tags);
		
		return t;
	}
	
	public synchronized void insertTransaction(Transaction t) throws SQLException {
		String query = "INSERT INTO finance_tracker.transactions (type, date, amount, description, account_id, category_id) VALUES (?, STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?, ?)";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, t.getType().toString());
		statement.setTimestamp(2, Timestamp.valueOf(t.getDate().withNano(0)));
		statement.setBigDecimal(3, t.getAmount());
		statement.setString(4, t.getDescription());
		statement.setLong(5, t.getAccount());
		statement.setLong(6, t.getCategory());
		
		try {
			dbManager.getConnection().setAutoCommit(false);
			statement.executeUpdate();
			
			ResultSet resultSet = statement.getGeneratedKeys();
			resultSet.next();
			t.setTransactionId(resultSet.getLong(1));
			
			for (Tag tag : t.getTags()) {
				tagDAO.insertTagToTags(tag, tag.getUserId());
				tagDAO.insertTagToTransaction(t, tag);
			}
			
			boolean existsBudget = budgetDao.existsBudget(t.getDate(), t.getCategory(), t.getAccount());
			Set<Budget> budgets = budgetDao.getAllBudgetsByDateCategoryAndAccount(t.getDate(), t.getCategory(), t.getAccount());
			if (existsBudget) {
				for (Budget budget : budgets) {
					budgetsHasTransactionsDAO.insertTransactionBudget(budget.getBudgetId(), t.getTransactionId());
					if (t.getType().equals(TransactionType.EXPENCE)) {
						budget.setAmount(budget.getAmount().add(t.getAmount()));
					}
					budgetDao.updateBudget(budget);
				}
			}
			
			t.setCategoryName(categoryDao.getCategoryNameByCategoryId(t.getCategory()));
			
			ALL_TRANSACTIONS.get(t.getType()).add(t);
			dbManager.getConnection().commit();
		} catch (SQLException e) {
			e.printStackTrace();
			dbManager.getConnection().rollback();
			throw new SQLException();
		} finally {
			dbManager.getConnection().setAutoCommit(true);
		}
	}
	
	public synchronized void updateTransaction(Transaction t) throws SQLException {
		String query = "UPDATE finance_tracker.transactions SET type = ?, date = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), amount = ?, description = ?, account_id = ?, category_id = ? WHERE transaction_id = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setString(1, t.getType().toString());
		statement.setTimestamp(2, Timestamp.valueOf(t.getDate().withNano(0)));
		statement.setBigDecimal(3, t.getAmount());
		statement.setString(4, t.getDescription());
		statement.setLong(5, t.getAccount());
		statement.setLong(6, t.getCategory());
		statement.setLong(7, t.getTransactionId());
		statement.executeUpdate();
		
		for (Tag tag : t.getTags()) {
			tagDAO.insertTagToTags(tag, tag.getUserId());
			tagDAO.insertTagToTransaction(t, tag);
		}
		
		removeTransaction(t.getTransactionId());
	
		t.setCategoryName(categoryDao.getCategoryNameByCategoryId(t.getCategory()));
		
		ALL_TRANSACTIONS.get(t.getType()).add(t);
	}
	
	public synchronized void deleteTransaction(Transaction t) throws SQLException {
		dbManager.getConnection().setAutoCommit(false);

		try {

			if (budgetDao.existsBudget(t.getDate(), t.getCategory(), t.getAccount())) {
				Set<Budget> budget = budgetDao.getAllBudgetsByDateCategoryAndAccount(t.getDate(), t.getCategory(), t.getAccount());
				
				for (Budget b : budget) {
					b.setAmount(b.getAmount().subtract(t.getAmount()));
					
					budgetDao.updateBudget(b);
				}
			}
			
			budgetsHasTransactionsDAO.deleteTransactionBudgetByTransactionId(t.getTransactionId());

			tagDAO.deleteAllTagsForTransaction(t.getTransactionId());
			
			String query = "DELETE FROM finance_tracker.transactions WHERE transaction_id = ?";
			PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
			statement.setLong(1, t.getTransactionId());
			statement.executeUpdate();
			
			ALL_TRANSACTIONS.get(t.getType()).remove(t);
			
			removeTransaction(t.getTransactionId());
			
			dbManager.getConnection().commit();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			
			dbManager.getConnection().rollback();
			
			throw new SQLException();
		} finally {
			dbManager.getConnection().setAutoCommit(true);
		}
	}
	
	public synchronized void removeTransaction(long transactionId) {
		for (ArrayList<Transaction> transactionTypes : ALL_TRANSACTIONS.values()) {
			for (Transaction transaction : transactionTypes) {
				if (transaction.getTransactionId() == transactionId) {
					ALL_TRANSACTIONS.get(transaction.getType()).remove(transaction);
					return;
				}
			}
		}
	}

	public boolean existsTransaction(LocalDateTime fromDate, LocalDateTime toDate, long categoryId, long accountId) throws SQLException {
		String sql = "SELECT type, date, account_id, category_id FROM transactions WHERE category_id = ? AND account_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, categoryId);
		ps.setLong(2, accountId);
		
		ResultSet res = ps.executeQuery();
		
		while (res.next()) {
			TransactionType type = TransactionType.valueOf(res.getString("type"));
			LocalDateTime date = res.getTimestamp("date").toLocalDateTime();
			
			if (type.equals(TransactionType.EXPENCE) && isBetweenTwoDates(date, fromDate, toDate)) {
				return true;
			}
		}
		
		return false;
	}

	private boolean isBetweenTwoDates(LocalDateTime date, LocalDateTime from, LocalDateTime to) {
		return !date.isBefore(from) && !date.isAfter(to);
	}

	public Set<Transaction> getAllTransactionsForBudget(LocalDateTime fromDate, LocalDateTime toDate, long categoryId,
			long accountId) throws SQLException {
		String sql = "SELECT transaction_id, type, date, description, amount, account_id, category_id FROM transactions WHERE category_id = ? AND account_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, categoryId);
		ps.setLong(2, accountId);
		
		ResultSet res = ps.executeQuery();
		
		Set<Transaction> transactions = new HashSet<>();
		
		while(res.next()) {
			long transactionId = res.getLong("transaction_id");
			TransactionType type = TransactionType.valueOf(res.getString("type"));
			LocalDateTime date = res.getTimestamp("date").toLocalDateTime();
			String description = res.getString("description");
			BigDecimal amount = res.getBigDecimal("amount");
			
			if (type.equals(TransactionType.EXPENCE) && isBetweenTwoDates(date, fromDate, toDate)) {
				Transaction t = new Transaction(transactionId, type, description, amount, accountId, categoryId, date, null);
				
				transactions.add(t);
			}
		}
		
		return transactions;
	}

	public List<Transaction> getAllTransactionsByUserId(long userId) throws SQLException {
		List<Transaction> transactions = new ArrayList<Transaction>();
		String query = "SELECT t.type, t.date, t.amount, t.account_id, t.category_id FROM finance_tracker.transactions t JOIN  finance_tracker.accounts a ON t.account_id = a.account_id WHERE a.user_id = ?";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, userId);
		
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			String type = result.getString("type");
			LocalDateTime date = result.getTimestamp("date").toLocalDateTime();
			BigDecimal amount = result.getBigDecimal("amount");
			long accountId = result.getLong("account_id");
			long categoryId = result.getLong("category_id");
			
			Transaction t = new Transaction(TransactionType.valueOf(type), date, amount, accountId, categoryId);
			transactions.add(t);
		}
		return transactions;
	}
	
	public TreeMap<String, BigDecimal> getAllCategoriesAndTheirAmountsByUserId(long userId, String type) throws SQLException {
		TreeMap<String, BigDecimal> categories = new TreeMap<String, BigDecimal>();
		String query = "SELECT SUM(t.amount) as amount, t.category_id FROM finance_tracker.transactions t JOIN finance_tracker.accounts a ON t.account_id = a.account_id JOIN finance_tracker.categories c on t.category_id = c.category_id WHERE (a.user_id = ? AND c.type = ?) group by category_id;";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, userId);
		statement.setString(2, type);
		
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			BigDecimal amount = result.getBigDecimal("amount");
			long categoryId = result.getLong("category_id");
			
			String categoryName = categoryDao.getCategoryNameByCategoryId(categoryId);
			categories.put(categoryName, amount);
		}
		
		return categories;
	}
	
	public Set<Transaction> getFilteredTransactions(long accountId, String type, long categoryId, LocalDateTime from, LocalDateTime to) throws SQLException {
		Set<Transaction> transactions = new HashSet<>();
		
		String sql = "SELECT transaction_id, date, amount, description FROM transactions "
						+ "WHERE account_id = ? AND category_id = ? AND date >= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s') "
																+  "AND date <= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s');";
		
		PreparedStatement statement = dbManager.getConnection().prepareStatement(sql);
		statement.setLong(1, accountId);
		statement.setLong(2, categoryId);
		statement.setTimestamp(3, Timestamp.valueOf(from.withNano(0)));
		statement.setTimestamp(4, Timestamp.valueOf(to.withNano(0)));
		
		ResultSet res = statement.executeQuery();
		
		while(res.next()) {
			long transactionId = res.getLong("transaction_id");
			LocalDateTime date = res.getTimestamp("date").toLocalDateTime();
			BigDecimal amount = res.getBigDecimal("amount");
			String description = res.getString("description");
			
			Transaction t = new Transaction(transactionId, TransactionType.valueOf(type), description, amount, accountId, categoryId, date, null);
			
			transactions.add(t);
		}
		
		return transactions;
	}
		
	public TreeMap<String, BigDecimal> getAllTransactionsByUserDateTypeAccount(long userId, LocalDateTime dateFrom, LocalDateTime dateTo, String type, String account) throws SQLException {
		TreeMap<String, BigDecimal> transactions = new TreeMap<String, BigDecimal>();
		String query = "SELECT t.category_id, SUM(t.amount) AS amount \r\n" + 
				"FROM finance_tracker.transactions t \r\n" + 
				"JOIN finance_tracker.accounts a ON t.account_id = a.account_id \r\n" + 
				"JOIN finance_tracker.categories c on t.category_id = c.category_id \r\n" + 
				"WHERE (a.user_id = ? AND c.type = ? AND (t.date BETWEEN ? AND ?) " + (account.equals("All accounts") ? ")" : "AND a.account_id = ? )") + 
				"group by category_id;";
		PreparedStatement statement = dbManager.getConnection().prepareStatement(query);
		statement.setLong(1, userId);
		statement.setString(2, type);
		statement.setTimestamp(3, Timestamp.valueOf(dateFrom.withNano(0)));
		statement.setTimestamp(4, Timestamp.valueOf(dateTo.withNano(0)));
		
		if (!account.equals("All accounts")) {
			Account a = accountDAO.getAccountByAccountNameAndAccountId(account, userId);
			statement.setLong(5, a.getAccountId());
		}
		
		ResultSet result = statement.executeQuery();
		while (result.next()) {
			long categoryId = result.getLong("category_id");
			BigDecimal amount = result.getBigDecimal("amount");
			
			String categoryName = categoryDao.getCategoryNameByCategoryId(categoryId);
			transactions.put(categoryName, amount);
		}
		
		return transactions;
	}
	
	public Map<String, BigDecimal> getIncomeVsExpences(long userId, long accountId, LocalDateTime ... dateArr) throws SQLException{
		Map<String, BigDecimal> result = new HashMap<>();
		result.put("INCOME", BigDecimal.valueOf(0));
		result.put("EXPENCE", BigDecimal.valueOf(0));
		
		String sql = "SELECT t.type, t.amount "
				+ "FROM transactions t "
				+ "JOIN accounts a "
				+ "ON t.account_id = a.account_id "
				+ "AND a.user_id = ? "
				+ (accountId == 0 ? "" : "AND a.account_id = ? ")
				+ (dateArr.length <= 0 ? "" : "WHERE t.date >= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s') "
											+ 	"AND t.date <= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s')") + ";";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, userId);
		if (accountId > 0) {
			ps.setLong(2, accountId);
			
			if (dateArr.length == 2) {
				ps.setTimestamp(3, Timestamp.valueOf(dateArr[0].withNano(0)));
				ps.setTimestamp(4, Timestamp.valueOf(dateArr[1].withNano(0)));
			}
		} else {
			if (dateArr.length == 2) {
				ps.setTimestamp(2, Timestamp.valueOf(dateArr[0].withNano(0)));
				ps.setTimestamp(3, Timestamp.valueOf(dateArr[1].withNano(0)));
			}
		}
		
		ResultSet res = ps.executeQuery();
		
		while(res.next()) {
			String type = res.getString("type");
			BigDecimal amount = res.getBigDecimal("amount");
			
			result.put(type, result.get(type).add(amount));
		}
				
		return result;
	}
	
	public Map<LocalDate, BigDecimal> getTransactionAmountAndDate(long userId, long accountId, LocalDateTime ... dateArr) throws SQLException {
		String sql = "SELECT t.type, t.amount, t.date "
				+ "FROM transactions t "
				+ "JOIN accounts a "
				+ "ON t.account_id = a.account_id "
				+ "AND a.user_id = ? "
				+ (accountId == 0 ? "" : "AND a.account_id = ? ")
				+ (dateArr.length <= 0 ? "" : "WHERE t.date >= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s') "
											+ 	"AND t.date <= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s')") + ";";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setLong(1, userId);
		if (accountId > 0) {
			ps.setLong(2, accountId);
			
			if (dateArr.length == 2) {
				ps.setTimestamp(3, Timestamp.valueOf(dateArr[0].withNano(0)));
				ps.setTimestamp(4, Timestamp.valueOf(dateArr[1].withNano(0)));
			}
		} else {
			if (dateArr.length == 2) {
				ps.setTimestamp(2, Timestamp.valueOf(dateArr[0].withNano(0)));
				ps.setTimestamp(3, Timestamp.valueOf(dateArr[1].withNano(0)));
			}
		}
		
		Map<LocalDate, BigDecimal> map = new TreeMap<>();
		
		ResultSet res = ps.executeQuery();
		
		while(res.next()) {
			String type = res.getString("type");
			BigDecimal amount = res.getBigDecimal("amount");
			LocalDateTime date = res.getTimestamp("date").toLocalDateTime();
			
			if (!map.containsKey(date.toLocalDate())) {
				map.put(date.toLocalDate(), type.equals("EXPENCE") ? amount.negate() : amount);
			} else {
				map.put(date.toLocalDate(), map.get(date.toLocalDate()).add(type.equals("EXPENCE") ? amount.negate() : amount));
			}
		}
		
		return map;
	}
	
	public Set<Transaction> getReportTransactions(long userId, String type, long categoryId, long accountId, LocalDateTime from, LocalDateTime to) throws SQLException {
		Set<Transaction> transactions = new HashSet<>();
		
		String sql = "SELECT * "
				+ "FROM transactions t "
				+ "JOIN accounts a ON t.account_id = a.account_id AND a.user_id = ? "
				+ "WHERE "
				+ (type.equals("All types") ? accountId == 0 ? "" : (categoryId == 0 ? "" : "category_id = ? AND ") + "a.account_id = ?" : "t.type = ?" + (accountId == 0 ? " AND " : " AND a.account_id = ? AND ")) 
				+ "t.date >= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s') " 
				+ "AND t.date <= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s')";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		
		ps.setLong(1, userId);
		
		if (type.equals("All types")) {
			if (accountId > 0) {
				ps.setLong(2, accountId);
				ps.setTimestamp(3, Timestamp.valueOf(from.withNano(0)));
				ps.setTimestamp(4, Timestamp.valueOf(to.withNano(0)));
			} else {
				ps.setTimestamp(2, Timestamp.valueOf(from.withNano(0)));
				ps.setTimestamp(3, Timestamp.valueOf(to.withNano(0)));
			}
		} else {
			ps.setString(2, type);
			if (categoryId > 0) {
				ps.setLong(3, categoryId);
				
				if (accountId > 0) {
					ps.setLong(4, accountId);
					ps.setTimestamp(5, Timestamp.valueOf(from.withNano(0)));
					ps.setTimestamp(6, Timestamp.valueOf(to.withNano(0)));
				} else {
					ps.setTimestamp(4, Timestamp.valueOf(from.withNano(0)));
					ps.setTimestamp(5, Timestamp.valueOf(to.withNano(0)));
				}
			} else {
				if (accountId > 0) {
					ps.setLong(3, accountId);
					ps.setTimestamp(4, Timestamp.valueOf(from.withNano(0)));
					ps.setTimestamp(5, Timestamp.valueOf(to.withNano(0)));
				} else {
					ps.setTimestamp(3, Timestamp.valueOf(from.withNano(0)));
					ps.setTimestamp(4, Timestamp.valueOf(to.withNano(0)));
				}
			}
		}
		
		ResultSet res = ps.executeQuery();

		while(res.next()) {
			long transactionId = res.getLong("transaction_id");
			String description = res.getString("description");
			String transactionType = res.getString("type");
			BigDecimal amount = res.getBigDecimal("amount");
			LocalDateTime date = res.getTimestamp("date").toLocalDateTime();
			long accId = res.getLong("account_id");
			long catId = res.getLong("category_id");
			
			Transaction t = new Transaction(transactionId, TransactionType.valueOf(transactionType), description, amount, accId, catId, date, null);
			
			t.setCategoryName(categoryDao.getCategoryByCategoryId(catId).getName());
		
			transactions.add(t);
		}
		
		return transactions;
	}
}
