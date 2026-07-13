package com.ssu.urlshortener.url.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public record CreateUrlRequest(

		@NotBlank(message = "원본 URL은 필수입니다.")
		String originalUrl,

		LocalDateTime expiresAt
) {
}