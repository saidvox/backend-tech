package com.techstore.backend.reminder.api;

import java.util.Map;

import com.techstore.backend.reminder.application.CustomerReminderService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recordatorios/clientes/ofertas")
public class CustomerReminderController {
	private final CustomerReminderService customerReminderService;

	public CustomerReminderController(CustomerReminderService customerReminderService) {
		this.customerReminderService = customerReminderService;
	}

	@PostMapping("/enviar")
	@PreAuthorize("hasRole('ADMIN')")
	public Map<String, Integer> sendNow() {
		return Map.of("sent", customerReminderService.sendDailyReminders());
	}
}
