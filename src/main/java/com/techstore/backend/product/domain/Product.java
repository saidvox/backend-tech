package com.techstore.backend.product.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.techstore.backend.category.domain.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "products")
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(name = "category", length = 80)
	private String categoryName;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(length = 1000)
	private String imageUrl;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private int stock;

	@Column(nullable = false)
	private boolean active = true;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Product() {
	}

	public Product(String name, Category category, String description, BigDecimal price, int stock, String imageUrl) {
		this.name = name;
		this.category = category;
		this.categoryName = category == null ? null : category.getName();
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.imageUrl = imageUrl;
	}

	public void update(String name, Category category, String description, BigDecimal price, int stock, boolean active, String imageUrl) {
		this.name = name;
		this.category = category;
		this.categoryName = category == null ? this.categoryName : category.getName();
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.active = active;
		this.imageUrl = imageUrl;
	}

	public void reduceStock(int quantity) {
		this.stock -= quantity;
	}

	public void increaseStock(int quantity) {
		this.stock += quantity;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category == null ? categoryName : category.getName();
	}

	public Long getCategoryId() {
		return category == null ? null : category.getId();
	}

	public String getCategoryName() {
		return categoryName;
	}

	public String getDescription() {
		return description;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public int getStock() {
		return stock;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
