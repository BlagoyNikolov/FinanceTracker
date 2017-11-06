package com.financeTracker.threads;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.financeTracker.model.User;
import com.financeTracker.model.db.UserDAO;
import com.financeTracker.util.EmailSender;

@Component
public class EmailService implements Runnable {

	@Autowired
	private UserDAO userDao;
	
	@Override
	public void run() {
		while (true) {
			try {
				List<User> allUsers = userDao.getUsersAndLastFillDate();
				for (User user : allUsers) {
					LocalDateTime lastFill = user.getLastFill();
					Period diff = Period.between(lastFill.toLocalDate(), LocalDate.now());
					System.out.println(user.getFirstName() + " " + diff.getDays());
					if (diff.getDays() > 7) {
//						EmailSender.sendSimpleEmail(user.getEmail(), "We miss you on Finance Tracker", ("Dear " + user.getFirstName() + ", We here at the Finance tracker noticed you have not used our service for some time now and miss you dearly.. Please come back we have wonderful things in store for you!"));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(24*60*60*1000); //24h*60m*60s*1000ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
