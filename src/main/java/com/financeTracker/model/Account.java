package com.financeTracker.model;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {

	private long accountId;
	
	@NotNull
	@Size(min = 2, max = 45)
	@NotEmpty
//	@Pattern(regexp="[^\\s]+")
	private String name;
	
	@NotNull
	@Min(1)
	@Max((long) 999999999.9999)
	private BigDecimal amount;
	
	private long userID;
	private List<Transaction> transactions;
	private List<Budget> budgets;
	private List<PlannedPayment> plannedPayments;
	
	public Account() {
		this.transactions = new ArrayList<>();
		this.budgets =  new ArrayList<>();
		this.plannedPayments =  new ArrayList<>();
	}
	
	public Account(String name, BigDecimal amount, long userID, List<Transaction> transactions, List<Budget> budgets, List<PlannedPayment> plannedPayments) {
		this.name = name;
		this.amount = amount;
		this.userID = userID;
		this.transactions = transactions;
		this.budgets = budgets;
		this.plannedPayments = plannedPayments;
	}
	
	public Account(String name, BigDecimal amount, long userID) {
		this(name, amount, userID, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	public String getName() {
		return name;
	}

	public BigDecimal getAmount() {
		return amount;
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
	
	public long getAccountId() {
		return accountId;
	}

	public long getUserId() {
		return userID;
	}

	public void setAccaountId(long accaountID) {
		this.accountId = accaountID;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public void setBudgets(List<Budget> budgets) {
		this.budgets = budgets;
	}

	public void setPlannedPayments(List<PlannedPayment> plannedPayments) {
		this.plannedPayments = plannedPayments;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (accountId ^ (accountId >>> 32));
		result = prime * result + ((amount == null) ? 0 : amount.hashCode());
		result = prime * result + ((budgets == null) ? 0 : budgets.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((plannedPayments == null) ? 0 : plannedPayments.hashCode());
		result = prime * result + ((transactions == null) ? 0 : transactions.hashCode());
		result = (int) (prime * result + userID);
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
		Account other = (Account) obj;
		if (accountId != other.accountId)
			return false;
		if (amount == null) {
			if (other.amount != null)
				return false;
		} else if (!amount.equals(other.amount))
			return false;
		if (budgets == null) {
			if (other.budgets != null)
				return false;
		} else if (!budgets.equals(other.budgets))
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
		if (userID != other.userID)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return name + " " + amount.toString();
	}
}
