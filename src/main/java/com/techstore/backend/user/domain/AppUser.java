package com.techstore.backend.user.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class AppUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, unique = true, length = 140)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean emailVerified = false;

	@Column(length = 255)
	private String emailVerificationCodeHash;

	@Column
	private Instant emailVerificationExpiresAt;

	@Column
	private Instant emailVerificationIssuedAt;

	@Column(length = 40)
	private String oauth2Provider;

	@Column(length = 160)
	private String oauth2ProviderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Role role = Role.USER;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected AppUser() {
	}

	public AppUser(String name, String email, String password, Role role) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	public void linkOAuth2Account(String provider, String providerId, String name) {
		this.oauth2Provider = provider;
		this.oauth2ProviderId = providerId;
		if (name != null && !name.isBlank()) {
			this.name = name;
		}
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerificationCode(String emailVerificationCodeHash, Instant emailVerificationExpiresAt, Instant emailVerificationIssuedAt) {
		this.emailVerificationCodeHash = emailVerificationCodeHash;
		this.emailVerificationExpiresAt = emailVerificationExpiresAt;
		this.emailVerificationIssuedAt = emailVerificationIssuedAt;
	}

	public String getEmailVerificationCodeHash() {
		return emailVerificationCodeHash;
	}

	public Instant getEmailVerificationExpiresAt() {
		return emailVerificationExpiresAt;
	}

	public Instant getEmailVerificationIssuedAt() {
		return emailVerificationIssuedAt;
	}

	public void markEmailVerified() {
		this.emailVerified = true;
		this.emailVerificationCodeHash = null;
		this.emailVerificationExpiresAt = null;
		this.emailVerificationIssuedAt = null;
	}

	public void markEmailUnverified() {
		this.emailVerified = false;
		this.emailVerificationCodeHash = null;
		this.emailVerificationExpiresAt = null;
		this.emailVerificationIssuedAt = null;
	}

	public String getOauth2Provider() {
		return oauth2Provider;
	}

	public String getOauth2ProviderId() {
		return oauth2ProviderId;
	}

	public Role getRole() {
		return role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
