package com.ssu.urlshortener.url.dto;

import java.time.LocalDateTime;

import com.ssu.urlshortener.url.entity.Url;

public record UrlResponse(
		Long id,
		String originalUrl,
		String shortCode,
		String shortUrl,
		long clickCount,
		LocalDateTime expiresAt,
		LocalDateTime createdAt
) {

	public static UrlResponse from(Url url, String baseUrl) {
		return new UrlResponse(
				url.getId(),
				url.getOriginalUrl(),
				url.getShortCode(),
				baseUrl + "/" + url.getShortCode(),
				url.getClickCount(),
				url.getExpiresAt(),
				url.getCreatedAt()
		);
	}
}