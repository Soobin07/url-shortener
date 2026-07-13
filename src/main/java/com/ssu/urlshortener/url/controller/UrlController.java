package com.ssu.urlshortener.url.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssu.urlshortener.url.dto.CreateUrlRequest;
import com.ssu.urlshortener.url.dto.UrlResponse;
import com.ssu.urlshortener.url.service.UrlService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

	private final UrlService urlService;

	@PostMapping
	public ResponseEntity<UrlResponse> create(
			@Valid @RequestBody CreateUrlRequest request
	) {
		UrlResponse response = urlService.create(request);

		return ResponseEntity
				.created(URI.create("/api/urls/" + response.shortCode()))
				.body(response);
	}
}