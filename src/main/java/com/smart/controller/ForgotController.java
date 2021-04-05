package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.service.EmailService;

@Controller
public class ForgotController {
	@Autowired
	private EmailService emailService;
	
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot-email-form";
	}
	@PostMapping("/sendotp")
public String otp(@RequestParam("email") String email, HttpSession session) {
		System.out.println("Email: "+email);
		Random rand=new Random(1000);
		int otp=rand.nextInt(99999);
		System.out.println("OTP is:"+otp);
		
		String subject="Otp from Aswin Smart Contact";
		String message="<h1> OTP="+otp+"</h1>";
		String to=email;
		
		boolean flag=this.emailService.sendEmail(subject, message, to);
		if (flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);

			return "verify-otp";
			
		}else {
			session.setAttribute("message", "please check your email Id");
			return "forgot-email-form";
		}
		
	}

}
