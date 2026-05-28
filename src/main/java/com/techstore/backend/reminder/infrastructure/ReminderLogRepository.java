package com.techstore.backend.reminder.infrastructure;

import java.time.LocalDate;

import com.techstore.backend.product.domain.Product;
import com.techstore.backend.reminder.domain.ReminderLog;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, Long> {
	boolean existsByUserAndProductAndReminderTypeAndReminderDate(
			AppUser user,
			Product product,
			String reminderType,
			LocalDate reminderDate);
}
