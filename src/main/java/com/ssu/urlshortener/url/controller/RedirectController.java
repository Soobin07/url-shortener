package com.ssu.urlshortener.url.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ssu.urlshortener.url.service.UrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedirectController {

	private final UrlService urlService;

	@GetMapping("/{shortCode:[A-Za-z0-9]{7}}")
	public ResponseEntity<Void> redirect(
			@PathVariable String shortCode
	) {
		String originalUrl = urlService.redirect(shortCode);

		return ResponseEntity
				.status(HttpStatus.FOUND)
				.location(URI.create(originalUrl))
				.build();
	}
}