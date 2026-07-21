package com.ssu.urlshortener.url.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssu.urlshortener.global.dto.PageResponse;
import com.ssu.urlshortener.url.dto.CreateUrlRequest;
import com.ssu.urlshortener.url.dto.UrlResponse;
import com.ssu.urlshortener.url.service.UrlService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;

import com.ssu.urlshortener.url.dto.UpdateUrlRequest;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlController {

	private final UrlService urlService;

	@PostMapping
	public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
		UrlResponse response = urlService.create(request);

		return ResponseEntity.created(URI.create("/api/urls/" + response.shortCode())).body(response);
	}

	@GetMapping
	public ResponseEntity<PageResponse<UrlResponse>> getUrls(@RequestParam(required = false) String keyword,
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		PageResponse<UrlResponse> response = urlService.getUrls(keyword, pageable);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{shortCode}")
	public ResponseEntity<UrlResponse> getUrl(@PathVariable String shortCode) {

		return ResponseEntity.ok(urlService.getUrl(shortCode));
	}

	@DeleteMapping("/{shortCode}")
	public ResponseEntity<Void> delete(@PathVariable String shortCode) {

		urlService.delete(shortCode);

		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{shortCode}")
	public ResponseEntity<UrlResponse> update(@PathVariable String shortCode,
			@Valid @RequestBody UpdateUrlRequest request) {
		UrlResponse response = urlService.update(shortCode, request);

		return ResponseEntity.ok(response);
	}
}