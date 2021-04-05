package com.smart.service;

import java.util.Properties;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;
@Service
public class EmailService {
	public boolean sendEmail(String subject, String message, String to) {
		boolean flag=false;
		String from="bharlarukha@gmail.com";
		String host="smtp.gmail.com";
		//get the system properties
		Properties properties=System.getProperties();
		System.out.println(properties);
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//Step 1: to get the session object..
		Session session=Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				
				return new PasswordAuthentication("bharlarukha@gmail.com", "Fatturai2");
			}
		});
		session.setDebug(true);
		//Step 2: compose the message[text,multimedia]
		MimeMessage m=new MimeMessage(session);
		try {
		m.setFrom(from);
		m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		//adding subject to message
		m.setSubject(subject);
		//adding text to message
		//m.setText(message);
		m.setContent(message, "text/html");
		//send
		//step 3: send message using transport class
		Transport.send(m);
		System.out.println("Send success...");
		flag=true;
		
	}catch(Exception e) {
		e.printStackTrace();}
		return flag;
	}
	
	
}
