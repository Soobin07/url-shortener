package com.ssu.urlshortener.url.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUrlRequest(

		@Size(max = 2048, message = "원본 URL은 2048자를 초과할 수 없습니다.") @Pattern(regexp = "^https?://.+$", message = "원본 URL은 http:// 또는 https://로 시작해야 합니다.") String originalUrl,

		@Future(message = "만료일은 현재보다 미래여야 합니다.") LocalDateTime expiresAt) {
}