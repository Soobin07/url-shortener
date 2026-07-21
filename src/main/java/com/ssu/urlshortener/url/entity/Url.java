package com.ssu.urlshortener.url.entity;

import java.time.LocalDateTime;

import com.ssu.urlshortener.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "urls")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Url extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String originalUrl;

	@Column(nullable = false, unique = true, length = 20)
	private String shortCode;

	@Column(nullable = false)
	private long clickCount;

	private LocalDateTime expiresAt;

	@Builder
	public Url(String originalUrl, String shortCode, LocalDateTime expiresAt) {
		this.originalUrl = originalUrl;
		this.shortCode = shortCode;
		this.expiresAt = expiresAt;
		this.clickCount = 0L;
	}

	public void increaseClickCount() {
		this.clickCount++;
	}

	public boolean isExpired(LocalDateTime now) {
		return expiresAt != null && expiresAt.isBefore(now);
	}

	public void update(String originalUrl, LocalDateTime expiresAt) {
		if (originalUrl != null) {
			this.originalUrl = originalUrl;
		}

		if (expiresAt != null) {
			this.expiresAt = expiresAt;
		}
	}
}