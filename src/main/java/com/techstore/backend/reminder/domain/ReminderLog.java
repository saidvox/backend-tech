package com.techstore.backend.reminder.domain;

import java.time.Instant;
import java.time.LocalDate;

import com.techstore.backend.product.domain.Product;
import com.techstore.backend.user.domain.AppUser;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reminder_logs", uniqueConstraints = @UniqueConstraint(columnNames = {
		"user_id", "product_id", "reminder_type", "reminder_date"
}))
public class ReminderLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(name = "reminder_type", nullable = false, length = 40)
	private String reminderType;

	@Column(name = "reminder_date", nullable = false)
	private LocalDate reminderDate;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected ReminderLog() {
	}

	public ReminderLog(AppUser user, Product product, String reminderType, LocalDate reminderDate) {
		this.user = user;
		this.product = product;
		this.reminderType = reminderType;
		this.reminderDate = reminderDate;
	}
}
