package com.tms.service;

import com.tms.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.io.File;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final TemplateEngine templateEngine;
	private final JavaMailSender javaMailSender;
	
	@Value("${spring.mail.username}")
	private String from;


	@Async
	public String sendMailWithAttachment(User user, String templateName, String subject, Map<Object, Object> data,
										 File file) throws MessagingException {
		Context context = new Context();
		context.setVariable("user", user);
		context.setVariable("password", data.get("data2"));
		context.setVariable("image", data.get("data1"));
		String process = templateEngine.process("emails/" + templateName, context);
		javax.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setFrom(from);
		helper.setSubject(subject);
		helper.setText(process, true);
		helper.setTo(user.getEmail());

		FileSystemResource fileSys = new FileSystemResource(file);

		helper.addAttachment("qr.jpg", fileSys);

		javaMailSender.send(mimeMessage);
		log.info("Mail sent successful to " + user.getName());
		return "Sent";

	}
	
	@Async
	public String sendMail(User user, String templateName, String subject, Map<Object, Object> data)
			throws MessagingException {
		Context context = new Context();
		context.setVariable("user", user);
		context.setVariable("password", data.get("data2"));
		context.setVariable("image", data.get("data1"));
		String process = templateEngine.process("emails/" + templateName, context);
		javax.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
		helper.setFrom(from);
		helper.setSubject(subject);
		helper.setText(process, true);
		helper.setTo(user.getEmail());
		javaMailSender.send(mimeMessage);
		log.info("Mail sent successful to " + user.getName());
		return "Sent";
	}
	
	@Async
	public String sendCredential(User user)
			throws MessagingException {
		Context context = new Context();
		context.setVariable("user", user);
		context.setVariable("password", user.getPassword());
		
		String process = templateEngine.process("emails/credential", context);
		javax.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
		helper.setFrom(from);
		helper.setSubject("TMS Login Credentials");
		helper.setText(process, true);
		helper.setTo(user.getEmail());
		javaMailSender.send(mimeMessage);
		log.info("Mail sent successful to " + user.getName());
		return "Sent";
	}
}
