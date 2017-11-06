package com.financeTracker.threads;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.financeTracker.model.Account;
import com.financeTracker.model.PlannedPayment;
import com.financeTracker.model.Tag;
import com.financeTracker.model.Transaction;
import com.financeTracker.model.TransactionType;
import com.financeTracker.model.db.AccountDAO;
import com.financeTracker.model.db.PlannedPaymentDAO;
import com.financeTracker.model.db.TransactionDAO;

@Component
@Scope("prototype")
public class PlannedPaymentThread implements Runnable {
	private PlannedPayment plannedPayment;
	private boolean running = true;
	
	@Autowired
	private AccountDAO accountDAO;
	
	@Autowired
	private TransactionDAO transactionDAO;
	
	@Autowired
	private PlannedPaymentDAO plannedPaymentDAO;
	
	
	public void setPlannedPayment(PlannedPayment plannedPayment) {
		this.plannedPayment = plannedPayment;
	}
	
	@Override
	public void run() {
		while (running) {
			Period diff = Period.between(LocalDate.now(), this.plannedPayment.getFromDate().toLocalDate());
			System.out.println("NOW " + LocalDate.now());
			System.out.println("THEN " + this.plannedPayment.getFromDate().toLocalDate());
			System.out.println("diff " + diff.getDays());
			//if is negative dont sleep
			if (!diff.isNegative()) {
				try {
					Thread.sleep(diff.getDays()*24*60*60*1000); //days*24h*60m*60s*1000ms
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					System.out.println("ACC ID: " + this.plannedPayment.getAccount());
					System.out.println("ACCOUNT DAO REF " + accountDAO);
					Account acc = accountDAO.getAccountByAccountId(this.plannedPayment.getAccount());
					BigDecimal newValue = this.plannedPayment.getAmount();
					BigDecimal oldValue = accountDAO.getAmountByAccountId(acc.getAccountId());
					Transaction t = null;
					if (this.plannedPayment.getPaymentType().equals(TransactionType.EXPENCE)) {
						accountDAO.updateAccountAmount(acc, oldValue.subtract(newValue));
						t = new Transaction(TransactionType.EXPENCE, "Planned Payment Expense", this.plannedPayment.getAmount(), this.plannedPayment.getAccount(), this.plannedPayment.getCategory(), LocalDateTime.now(), this.plannedPayment.getTags());
					} else 
					if (this.plannedPayment.getPaymentType().equals(TransactionType.INCOME)) {
						accountDAO.updateAccountAmount(acc, oldValue.add(newValue));
						t = new Transaction(TransactionType.INCOME, "Planned Payment Income", this.plannedPayment.getAmount(), this.plannedPayment.getAccount(), this.plannedPayment.getCategory(), LocalDateTime.now(), this.plannedPayment.getTags());
					}
					transactionDAO.insertTransaction(t);
					
					plannedPaymentDAO.deletePlannedPayment(this.plannedPayment.getPlannedPaymentId());
					running = false;
					System.err.println("pp deleted");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} 
		}
	}
}
