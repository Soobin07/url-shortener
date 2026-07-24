package com.ssu.urlshortener.url.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "단축 URL 생성 요청")
public record CreateUrlRequest(

		@Schema(
				description = "단축할 원본 URL",
				example = "https://example.com/articles/123",
				requiredMode = Schema.RequiredMode.REQUIRED,
				maxLength = 2048
		)
		@NotBlank(message = "원본 URL은 필수입니다.")
		@Size(max = 2048, message = "원본 URL은 2048자를 초과할 수 없습니다.")
		@Pattern(
				regexp = "^https?://.+$",
				message = "원본 URL은 http:// 또는 https://로 시작해야 합니다."
		)
		String originalUrl,

		@Schema(
				description = "단축 URL 만료일. 생략하면 만료되지 않습니다.",
				example = "2027-12-31T23:59:59",
				nullable = true
		)
		@Future(message = "만료일은 현재보다 미래여야 합니다.")
		LocalDateTime expiresAt
) {
}