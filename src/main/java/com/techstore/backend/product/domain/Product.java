package com.techstore.backend.product.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

	@Column(precision = 10, scale = 2)
	private BigDecimal offerPrice;

	@Column
	private Instant offerStartsAt;

	@Column
	private Instant offerEndsAt;

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
		update(name, category, description, price, stock, active, imageUrl, offerPrice, offerStartsAt, offerEndsAt);
	}

	public void update(
			String name,
			Category category,
			String description,
			BigDecimal price,
			int stock,
			boolean active,
			String imageUrl,
			BigDecimal offerPrice,
			Instant offerStartsAt,
			Instant offerEndsAt) {
		this.name = name;
		this.category = category;
		this.categoryName = category == null ? this.categoryName : category.getName();
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.active = active;
		this.imageUrl = imageUrl;
		this.offerPrice = offerPrice;
		this.offerStartsAt = offerStartsAt;
		this.offerEndsAt = offerEndsAt;
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

	public BigDecimal getOfferPrice() {
		return offerPrice;
	}

	public Instant getOfferStartsAt() {
		return offerStartsAt;
	}

	public Instant getOfferEndsAt() {
		return offerEndsAt;
	}

	public boolean isOnOffer() {
		if (offerPrice == null || price == null || offerPrice.compareTo(BigDecimal.ZERO) <= 0 || offerPrice.compareTo(price) >= 0) {
			return false;
		}
		Instant now = Instant.now();
		boolean startsOk = offerStartsAt == null || !now.isBefore(offerStartsAt);
		boolean endsOk = offerEndsAt == null || now.isBefore(offerEndsAt);
		return startsOk && endsOk;
	}

	public BigDecimal getEffectivePrice() {
		return isOnOffer() ? offerPrice : price;
	}

	public Integer getDiscountPercentage() {
		if (!isOnOffer()) {
			return null;
		}
		BigDecimal discount = price.subtract(offerPrice)
				.multiply(BigDecimal.valueOf(100))
				.divide(price, 0, RoundingMode.HALF_UP);
		return discount.intValue();
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
