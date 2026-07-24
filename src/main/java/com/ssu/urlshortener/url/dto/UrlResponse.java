package com.ssu.urlshortener.url.dto;

import java.time.LocalDateTime;

import com.ssu.urlshortener.url.entity.Url;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "URL 정보 응답")
public record UrlResponse(
		@Schema(
				description = "URL 식별자",
				example = "1"
		)
		Long id,
		@Schema(
				description = "원본 URL",
				example = "https://example.com/articles/123"
		)
		String originalUrl,
		@Schema(
				description = "단축 코드",
				example = "AbCd12"
		)
		String shortCode,
		@Schema(
				description = "완성된 단축 URL",
				example = "http://localhost:8080/AbCd12"
		)
		String shortUrl,
		@Schema(
				description = "리다이렉트 클릭 수",
				example = "5"
		)
		long clickCount,
		@Schema(
				description = "만료일",
				example = "2027-12-31T23:59:59",
				nullable = true
		)
		LocalDateTime expiresAt,
		@Schema(
				description = "생성 일시",
				example = "2026-07-24T20:00:00"
		)
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