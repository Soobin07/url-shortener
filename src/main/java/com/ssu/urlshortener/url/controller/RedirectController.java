package com.ssu.urlshortener.url.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ssu.urlshortener.url.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(
		name = "Redirect",
		description = "단축 URL 리다이렉트 API"
)
@RestController
@RequiredArgsConstructor
public class RedirectController {

	private final UrlService urlService;

	@Operation(
			summary = "원본 URL로 리다이렉트",
			description = """
					단축 코드에 대응하는 원본 URL로 리다이렉트합니다.

					정상적인 요청이면 클릭 수가 1 증가합니다.
					"""
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "302",
					description = "원본 URL로 리다이렉트"
			),
			@ApiResponse(
					responseCode = "404",
					description = "존재하지 않는 단축 코드"
			),
			@ApiResponse(
					responseCode = "410",
					description = "만료된 단축 URL"
			)
	})
	@GetMapping("/{shortCode:[A-Za-z0-9]{7}}")
	public ResponseEntity<Void> redirect(
			@Parameter(
					description = "리다이렉트할 단축 코드",
					example = "AbCd12"
			)
			@PathVariable String shortCode
	) {
		String originalUrl = urlService.redirect(shortCode);

		return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(URI.create(originalUrl))
				.build();
	}
}