package com.techstore.backend.favorite.domain;

import java.time.Instant;

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
@Table(name = "favorite_products", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "product_id" }))
public class FavoriteProduct {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected FavoriteProduct() {
	}

	public FavoriteProduct(AppUser user, Product product) {
		this.user = user;
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}

	public AppUser getUser() {
		return user;
	}
}
