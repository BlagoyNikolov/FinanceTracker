package com.financeTracker.util;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender {
	private static final String MEDELIN_EMAIL = "ittfinancetracker@gmail.com";
	private static final String MEDELIN_PASS = "parolaft";
	
	public static void sendEmailWithattachment(String receiverEmail, String subjectText, String msgText,
			String fileName) {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(MEDELIN_EMAIL, MEDELIN_PASS);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(MEDELIN_EMAIL));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
			message.setSubject(subjectText);

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText(msgText);

			Multipart multipart = new MimeMultipart();

			multipart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(fileName); // the path
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(fileName); // the path
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);

			Transport.send(message);

			System.out.println("Email sent.");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void sendSimpleEmail(String receiverEmail, String subjectText, String msgText) {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(MEDELIN_EMAIL, MEDELIN_PASS);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("project.medelin@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiverEmail));
			message.setSubject(subjectText);
			message.setText(msgText);

			Transport.send(message);

			System.out.println("Email sent.");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

	}
}
