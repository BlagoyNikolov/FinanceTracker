package com.financeTracker.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

public class Budget {
	private long budgetId;
	
	@NotNull
	@Size(min = 2, max = 45)
	@NotEmpty
	private String name;

	@NotNull
	@Min(1)
	@Max((long) 999999999.9999)
	private BigDecimal initialAmount;
	private BigDecimal amount;
	private LocalDateTime fromDate;
	private LocalDateTime toDate;
	private long accountId;
	private long categoryId;
	private Set<Tag> tags = new HashSet<Tag>();
	private Set<Transaction> transactions = new HashSet<Transaction>();
	
	public Budget() {}
	
	public Budget(long budgetId, String name, BigDecimal initialAmount, BigDecimal amount, LocalDateTime fromDate, LocalDateTime toDate,
			long accountId, long categoryId, Set<Tag> tags, Set<Transaction> transactions) {
		this(name, initialAmount, amount, fromDate, toDate, accountId, categoryId, tags, transactions);
		
		this.budgetId = budgetId;
	}
	
	public Budget(long budgetId, String name, BigDecimal initialAmount, LocalDateTime fromDate, LocalDateTime toDate,
			long accountId, long categoryId) {
		this(budgetId, name, initialAmount, BigDecimal.valueOf(0), fromDate, toDate, accountId, categoryId, new HashSet<>(), new HashSet<>());
	}
	
	public Budget(String name, BigDecimal initialAmount, LocalDateTime fromDate, LocalDateTime toDate,
			long accountId, long categoryId, Set<Tag> tags) {
		this(name, initialAmount, BigDecimal.valueOf(0), fromDate, toDate, accountId, categoryId, tags, new HashSet<>());
	}
	
	public Budget(long budgetId, String name, BigDecimal initialAmount, BigDecimal amount, LocalDateTime fromDate, LocalDateTime toDate,
			long accountId, long categoryId, Set<Tag> tags) {
		this(name, initialAmount, amount, fromDate, toDate, accountId, categoryId, tags, new HashSet<>());
		
		this.budgetId = budgetId;
	}

	public Budget(String name, BigDecimal initialAmount, BigDecimal amount, LocalDateTime fromDate, LocalDateTime toDate,
			long accountId, long categoryId, Set<Tag> tags, Set<Transaction> transactions) {
		this.name = name;
		this.initialAmount = initialAmount;
		this.amount = amount;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.accountId = accountId;
		this.categoryId = categoryId;
		this.tags = tags;
		this.transactions = transactions;
	}
	
	public long getBudgetId() {
		return budgetId;
	}
	
	public void setBudgetId(long budgetId) {
		this.budgetId = budgetId;
	}
	
	public String getName() {
		return name;
	}
	
	public BigDecimal getInitialAmount() {
		return initialAmount;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public LocalDateTime getFromDate() {
		return fromDate;
	}
	
	public LocalDateTime getToDate() {
		return toDate;
	}
	
	public long getAccountId() {
		return accountId;
	}
	
	public long getCategoryId() {
		return categoryId;
	}
	
	public Set<Tag> getTags() {
		return Collections.unmodifiableSet(tags);
	}
	
	public Set<Transaction> getTransactions() {
		return Collections.unmodifiableSet(transactions);
	}
	
	public void setTransactions(Set<Transaction> transactions) {
		this.transactions = transactions;
	}

	
	public void setName(String name) {
		this.name = name.trim();
	}

	public void setInitialAmount(BigDecimal initialAmount) {
		this.initialAmount = initialAmount;
	}

	public void setFromDate(LocalDateTime fromDate) {
		this.fromDate = fromDate;
	}

	public void setToDate(LocalDateTime toDate) {
		this.toDate = toDate;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
	
	@Override
	public String toString() {
		return "Budget [budgetId=" + budgetId + ", name=" + name + ", amount=" + amount + ", fromDate=" + fromDate
				+ ", toDate=" + toDate + ", accountId=" + accountId + ", categoryId=" + categoryId + ", tags=" + tags + "]\n";
	}
}
