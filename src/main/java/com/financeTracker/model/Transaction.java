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

public class Transaction {
	private long transactionId;
	private TransactionType type;
	
	@Size(min = 2, max = 45)
	private String description;
	
	@NotNull
	@Min(1)
	@Max((long) 999999999.9999)
	private BigDecimal amount;
	
	private long account;
	private String categoryName;
	private long category;
	private LocalDateTime date;
	private Set<Tag> tags = new HashSet<Tag>();
	
	public Transaction() {
	}
	
	public Transaction(long transactionId, TransactionType type, String description, BigDecimal amount, long account, long category,
			LocalDateTime date, Set<Tag> tags) {
		this(type, description, amount,account, category, date, tags);
		
		this.transactionId = transactionId;
	}

	public Transaction(TransactionType type, String description, BigDecimal amount, long account, long category, LocalDateTime date, Set<Tag> tags) {
		this.type = type;
		this.description = description;
		this.amount = amount;
		this.account = account;
		this.category = category;
		this.date = date;
		this.tags = tags;
	}
	
	public Transaction(TransactionType type, LocalDateTime date, BigDecimal amount, long account, long category) {
		this.type = type;
		this.date = date;
		this.amount = amount;
		this.account = account;
		this.category = category;
	}
	
	public long getTransactionId() {
		return transactionId;
	}
	
	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionType getType() {
		return type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public long getAccount() {
		return account;
	}
	
	public String getCategoryName() {
		return categoryName;
	}
	
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	
	public long getCategory() {
		return category;
	}
	
	public LocalDateTime getDate() {
		return date;
	}
	
	public Set<Tag> getTags() {
		return Collections.unmodifiableSet(tags);
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	@Override
	public String toString() {
		return "Transaction [transactionId=" + transactionId + ", type=" + type + ", amount=" + amount + ", account="
				+ account + ", category=" + category + ", date=" + date + ", tags="
				+ tags + "]\n";
	}
}
