package com.financeTracker.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

public class Category {

	private long categoryId;
	
	@NotNull
	@Size(min = 2, max = 45)
	@NotEmpty
	@Pattern(regexp="[^\\s]+")
	private String name;
	
	private TransactionType type;
	private Long userId;
	private List<Transaction> transactions;
	private List<Budget> budgets;
	private List<PlannedPayment> plannedPayments;
	
	public Category() {
		this.transactions = new ArrayList<>();
		this.budgets = new ArrayList<>();
		this.plannedPayments = new ArrayList<>();
	}
	
	public Category(String name, TransactionType type , Long userId, List<Transaction> transactions, List<Budget> budgets, List<PlannedPayment> plannedPayments) {
		this.name = name;
		this.type = type;
		this.userId = userId;
		this.transactions = transactions;
		this.budgets = budgets;
		this.plannedPayments = plannedPayments;
		
	}

	public Long getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public TransactionType getType() {
		return type;
	}
	
	public long getCategoryId() {
		return categoryId;
	}
	
	public List<Transaction> getTransactions() {
		return Collections.unmodifiableList(transactions);
	}
	
	public List<Budget> getBudgets() {
		return Collections.unmodifiableList(budgets);
	}
	
	public List<PlannedPayment> getPlannedPayments() {
		return Collections.unmodifiableList(plannedPayments);
	}
	
	public void setCategoryID(long categoryID) {
		this.categoryId = categoryID;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public void setPlannedPayments(List<PlannedPayment> plannedPayments) {
		this.plannedPayments = plannedPayments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((budgets == null) ? 0 : budgets.hashCode());
		result = prime * result + (int) (categoryId ^ (categoryId >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((plannedPayments == null) ? 0 : plannedPayments.hashCode());
		result = prime * result + ((transactions == null) ? 0 : transactions.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (budgets == null) {
			if (other.budgets != null)
				return false;
		} else if (!budgets.equals(other.budgets))
			return false;
		if (categoryId != other.categoryId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (plannedPayments == null) {
			if (other.plannedPayments != null)
				return false;
		} else if (!plannedPayments.equals(other.plannedPayments))
			return false;
		if (transactions == null) {
			if (other.transactions != null)
				return false;
		} else if (!transactions.equals(other.transactions))
			return false;
		if (type != other.type)
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}
