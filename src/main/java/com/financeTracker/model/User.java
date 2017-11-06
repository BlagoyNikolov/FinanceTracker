package com.financeTracker.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

public class User {

	private long userId;
	
	@NotNull
	@Size(min = 3, max = 45)
	@NotEmpty
	@Pattern(regexp = "[^\\s]+")
	private String username;
	
	@NotNull
	@NotEmpty
	private byte[] password;
	
	@NotNull
	@NotEmpty
	@Email
	@Pattern(regexp = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")
	private String email;
	
	@NotNull
	@Size(min = 3, max = 45)
	@NotEmpty
	@Pattern(regexp = "[^\\s]+")
	private String firstName;
	
	@NotNull
	@Size(min = 3, max = 45)
	@NotEmpty
	@Pattern(regexp = "[^\\s]+")
	private String lastName;
	
	private LocalDateTime lastFill;
	private String passwordToken;
	private Set<Account> accounts;
	private Set<Category> ownCategories;
	private Set<Tag> tags;
	
	public User() {
		this.accounts = new HashSet<>();
		this.ownCategories = new HashSet<>();
		this.tags = new HashSet<>();
	}
	
	public User(String username, String password, String email, String firstName, String lastName, Set<Account> accounts, 
			Set<Category> ownCategories, Set<Tag> tags, LocalDateTime lastFill, String passwordToken) {
		this.username = username;
		this.password = DigestUtils.sha512(password);
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.accounts = accounts;
		this.ownCategories = ownCategories;
		this.tags = tags;
		this.lastFill = lastFill;
		this.passwordToken = passwordToken;
	}
	
	public User(String username, String password, String email, String firstName, String lastName) {
		this(username, password, email, firstName, lastName, new HashSet<>(), new HashSet<>(), new HashSet<>(), LocalDateTime.now(),
				DigestUtils.sha512Hex(DigestUtils.sha512Hex(password) + username));
		
	}
	
	public User(String email, String firstName, LocalDateTime lastFill) {
		this.email = email;
		this.firstName = firstName;
		this.lastFill = lastFill;
	}

	public long getUserId() {
		return userId;
	}
	
	public String getUsername() {
		return username;
	}

	public byte[] getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Set<Account> getAccounts() {
		return Collections.unmodifiableSet(accounts);
	}

	public Set<Category> getOwnCategories() {
		return Collections.unmodifiableSet(ownCategories);
	}

	public Set<Tag> getTags() {
		return Collections.unmodifiableSet(tags);
	}

	public void setUserId(long userID) {
		this.userId = userID;
	}
	
	public LocalDateTime getLastFill() {
		return lastFill;
	}
	
	public void setLastFill(LocalDateTime lastFill) {
		this.lastFill = lastFill;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName.trim();
	}

	public void setLastName(String lastName) {
		this.lastName = lastName.trim();
	}

	public void setUsername(String username) {
		this.username = username.trim();
	}

	public void setPassword(String password) {
		this.password = DigestUtils.sha512(password.trim());
	}
	
	public void addAccount(Account acc) {
		this.accounts.add(acc);
	}
	
	public void removeAccount(Account acc) {
		this.accounts.remove(acc);
	}

	public String getPasswordToken() {
		return passwordToken;
	}

	public void setPasswordToken(String passwordToken) {
		this.passwordToken = passwordToken;
	}
}
