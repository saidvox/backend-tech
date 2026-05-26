package com.techstore.backend.config.mail;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class MailSenderPasswordSanitizer implements BeanPostProcessor {
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof JavaMailSenderImpl mailSender && mailSender.getPassword() != null) {
			mailSender.setPassword(mailSender.getPassword().replaceAll("\\s+", ""));
		}
		return bean;
	}
}
