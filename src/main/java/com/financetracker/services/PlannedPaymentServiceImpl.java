package com.financetracker.services;

import com.financetracker.model.*;
import com.financetracker.repositories.PlannedPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlannedPaymentServiceImpl implements PlannedPaymentService {

    public static final String PLANNED_PAYMENT_EXPENSE = "Planned Payment Expense";
    public static final String PLANNED_PAYMENT_INCOME = "Planned Payment Income";

    @Autowired
    private PlannedPaymentRepository plannedPaymentRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionService transactionService;

    public List<PlannedPayment> getAllPlannedPayments() {
        return plannedPaymentRepository.findAll();
    }

    public List<PlannedPayment> getAllPlannedPaymentsByAccountId(long accountId) {
        return plannedPaymentRepository.findByAccountAccountId(accountId);
    }

    public List<PlannedPayment> getAllPlannedPaymentsByCategoryId(long categoryId) {
        return plannedPaymentRepository.findByCategoryCategoryId(categoryId);
    }

    public void insertPlannedPayment(PlannedPayment plannedPayment) {
        plannedPaymentRepository.save(plannedPayment);
    }

    public void updatePlannedPayment(PlannedPayment plannedPayment) {
        plannedPaymentRepository.save(plannedPayment);
    }

    public void postPlannedPayment(User user, String account, String category, String name, String type, LocalDateTime date, String amount,
                                   PlannedPayment plannedPayment, long plannedPaymentId) {

        Account acc = accountService.getAccountByUserAndAccountName(user, account);
        Category cat = categoryService.getCategoryByCategoryName(category);
        PlannedPayment payment = new PlannedPayment(name, PaymentType.valueOf(type), date,
                BigDecimal.valueOf(Double.valueOf(amount)), plannedPayment.getDescription(), acc, cat);

        if (plannedPaymentId != 0) {
            payment.setPlannedPaymentId(plannedPaymentId);
            updatePlannedPayment(payment);
        } else {
            insertPlannedPayment(payment);
        }
    }

    public void deletePlannedPayment(long plannedPaymentId) {
        plannedPaymentRepository.delete(plannedPaymentId);
    }

    public PlannedPayment getPlannedPaymentByPlannedPaymentId(long plannedPaymentId) {
        return plannedPaymentRepository.findByPlannedPaymentId(plannedPaymentId);
    }

    public List<PlannedPayment> getAllPlannedPaymentsByUser(User user) {
        return plannedPaymentRepository.findByAccountUser(user);
    }

    @Scheduled(cron = "0 38 23 * * ?", zone = "Europe/Athens") //Fire at 9:00am every day
    public void plannedPaymentDailyCronJob() {
        LocalDate localDate = LocalDateTime.now().toLocalDate();
        LocalDateTime localDateTime = LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), 0, 0, 0);

        List<PlannedPayment> plannedPaymentsWithCurrentDate = plannedPaymentRepository.findAllByFromDate(localDateTime);
        for (PlannedPayment plannedPayment : plannedPaymentsWithCurrentDate) {
           this.executePlannedPayment(plannedPayment);
        }
    }

    private void executePlannedPayment(PlannedPayment plannedPayment) {
        Account acc = accountService.getAccountByAccountId(plannedPayment.getAccount().getAccountId());
        BigDecimal newValue = plannedPayment.getAmount();
        BigDecimal oldValue = accountService.getAmountByAccountId(acc.getAccountId());
        Transaction transaction = null;

        if (plannedPayment.getPaymentType().equals(PaymentType.EXPENSE)) {
            acc.setAmount(oldValue.subtract(newValue));
            accountService.updateAccount(acc);
            transaction = Transaction.createTransactionByPlannedPayment(PaymentType.EXPENSE, PLANNED_PAYMENT_EXPENSE, plannedPayment);
        } else if (plannedPayment.getPaymentType().equals(PaymentType.INCOME)) {
            acc.setAmount(oldValue.add(newValue));
            accountService.updateAccount(acc);
            transaction = Transaction.createTransactionByPlannedPayment(PaymentType.INCOME, PLANNED_PAYMENT_INCOME, plannedPayment);
        }
        transactionService.insertTransaction(transaction);
        this.deletePlannedPayment(plannedPayment.getPlannedPaymentId());
    }
}
