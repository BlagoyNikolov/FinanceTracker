package com.financeTracker.model.db;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.financeTracker.model.Tag;

import com.financeTracker.model.Account;
import com.financeTracker.model.Category;
import com.financeTracker.model.User;

@Component
public class UserDAO {
	@Autowired
	private DBManager dbManager;
	
	@Autowired
	private CategoryDAO categoryDao;
	
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private TagDAO tagDAO;
	
	private static final Map<String, User> ALL_USERS = new HashMap<>();
	
	@PostConstruct
	private void init() throws SQLException {
		getAllUsers();
	}
	
	private void getAllUsers() throws SQLException {
		if (!ALL_USERS.isEmpty()) {
			return;
		}
		
		String sql = "SELECT user_id, username, password, email, first_name, last_name, last_fill, password_token FROM users";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ResultSet res = ps.executeQuery();
		
		while(res.next()) {
			String userName = res.getString("username");
			String password = res.getString("password");
			String email = res.getString("email");
			String firstName = res.getString("first_name");
			String lastName = res.getString("last_name");
			Long userId = Long.valueOf(res.getLong("user_id"));
			LocalDateTime lastFill = res.getTimestamp("last_fill").toLocalDateTime();
			String passwordToken = res.getString("password_token");
			Set<Account> accounts = accountDAO.getAllAccountsByUserId(userId);
			Set<Category> ownCategories = categoryDao.getAllCategoriesByUserId(userId);
			Set<Tag> tags = tagDAO.getAllTagsByUserId(userId);
			
			User user = new User(userName, password, email, firstName, lastName, accounts, ownCategories, tags, lastFill, passwordToken);
			user.setUserId(userId);
			
			ALL_USERS.put(userName, user);
		}
	}
	
	public List<User> getUsersAndLastFillDate() throws SQLException {
		List<User> users = new ArrayList<User>();
		String query = "SELECT email, first_name, last_fill FROM users";
		PreparedStatement ps = dbManager.getConnection().prepareStatement(query);
		ResultSet res = ps.executeQuery();
		while(res.next()) {
			String email = res.getString("email");
			String firstName = res.getString("first_name");
			LocalDateTime lastFill = res.getTimestamp("last_fill").toLocalDateTime();
			
			User user = new User(email, firstName, lastFill);
			users.add(user);
		}
		return users;
	}

	public synchronized void insertUser(User u) throws SQLException {
		Connection con = dbManager.getConnection();
		PreparedStatement ps = con.prepareStatement("INSERT INTO users (username, password, email, first_name, last_name, last_fill, password_token) "
														+ "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		
		ps.setString(1, u.getUsername());
		ps.setString(2, DigestUtils.sha512Hex(u.getPassword()));
		ps.setString(3, u.getEmail());
		ps.setString(4, u.getFirstName());
		ps.setString(5, u.getLastName());
		ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now().withNano(0)));
		ps.setString(7, u.getPasswordToken());
		ps.executeUpdate();
		
		ResultSet res = ps.getGeneratedKeys();
		res.next();
		
		u.setUserId(res.getLong(1));
		u.setPassword(DigestUtils.sha512Hex(u.getPassword()));
		
		ALL_USERS.put(u.getUsername(), u);
	}
	
	public synchronized void updateUser(User user) throws SQLException {
		String sql = "UPDATE users SET email = ?, first_name = ?, last_name = ?, "
				+ "last_fill = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), password_token = ? WHERE user_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, user.getEmail());
		ps.setString(2, user.getFirstName());
		ps.setString(3, user.getLastName());
		ps.setTimestamp(4, Timestamp.valueOf(user.getLastFill().withNano(0)));
		ps.setString(5, user.getPasswordToken());
		ps.setLong(6, user.getUserId());
		ps.executeUpdate();
		
		ALL_USERS.put(user.getUsername(), user);
	}
	
	public synchronized void updateUserPassword(User user, String password) throws SQLException {
		String sql = "UPDATE users SET password = ? WHERE user_id = ?;";
		
		String pass = DigestUtils.sha512Hex(DigestUtils.sha512(password));
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, pass);
		ps.setLong(2, user.getUserId());
		ps.executeUpdate();
		
		user.setPassword(pass);
		
		ALL_USERS.put(user.getUsername(), user);
	}
	
	public synchronized User getUserByUserId(int userId) {
		if (!ALL_USERS.isEmpty()) {
			for (User u : ALL_USERS.values()) {
				if (u.getUserId() == userId) {
					return u;
				}
			}
		}
		
		return null;
	}
	
	public synchronized int getUserId(String username) {		
		int id = 0;
		
		if (ALL_USERS.containsKey(username)) {
			id = (int)ALL_USERS.get(username).getUserId();
		}
		
		return id;
	}
	
	public synchronized User getUser(String username) {
		User user = null;
		
		if (ALL_USERS.containsKey(username)) {
			user = ALL_USERS.get(username);
		}
		
		return user;
	}
	
	public synchronized boolean existsUser(String username) throws SQLException {
		String sql = "SELECT count(*) as count FROM users WHERE username = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, username);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		return res.getInt("count") > 0;
	}
	
	public synchronized void deleteUser(String username) throws SQLException {
		String sql = "DELETE FROM users WHERE user_id = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1,  username);
		ps.executeUpdate();
		
		ALL_USERS.remove(username);
	}
	
	
	public synchronized boolean isValidLogin(String username, byte[] password) throws SQLException {
		byte[] hashedPassword = DigestUtils.sha512(DigestUtils.sha512Hex(password));
		
		if (ALL_USERS.containsKey(username)) {
			User user = ALL_USERS.get(username);
			
			return MessageDigest.isEqual(hashedPassword, user.getPassword());
		}
		
		return false;
	}

	public boolean existEmail(String email) throws SQLException {
		String sql = "SELECT count(*) as count FROM users WHERE email = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, email);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		return res.getInt("count") > 0;
	}

	public User getUserByEmail(String email) throws SQLException {
		String sql = "SELECT user_id, username, password, first_name, last_name, last_fill, password_token FROM users WHERE email = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, email);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		String username = res.getString("username");
		String password = res.getString("password");
		String firstName = res.getString("first_name");
		String lastName = res.getString("last_name");
		long userId = res.getLong("user_id");
		LocalDateTime lastFill = res.getTimestamp("last_fill").toLocalDateTime();
		String passwordToken = res.getString("password_token");
		
		User user = new User(username, password, email, firstName, lastName, null, null, null, lastFill, passwordToken);
		user.setUserId(userId);
		
		return user;
	}
	
	public boolean existUserByPasswordToken(String token) throws SQLException {
		String sql = "SELECT count(*) as count FROM users WHERE password_token = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, token);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		return res.getInt("count") > 0;
	}
	
	public User getUserByPasswordToken(String token) throws SQLException {
		String sql = "SELECT user_id, username, password, first_name, last_name, last_fill, email FROM users WHERE password_token = ?;";
		
		PreparedStatement ps = dbManager.getConnection().prepareStatement(sql);
		ps.setString(1, token);
		
		ResultSet res = ps.executeQuery();
		res.next();
		
		String username = res.getString("username");
		String password = res.getString("password");
		String firstName = res.getString("first_name");
		String lastName = res.getString("last_name");
		long userId = res.getLong("user_id");
		LocalDateTime lastFill = res.getTimestamp("last_fill").toLocalDateTime();
		String email = res.getString("email");
		
		User user = new User(username, password, email, firstName, lastName, null, null, null, lastFill, token);
		user.setUserId(userId);
		
		return user;
	}
}
