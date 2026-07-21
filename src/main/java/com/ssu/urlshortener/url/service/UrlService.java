package com.ssu.urlshortener.url.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssu.urlshortener.global.dto.PageResponse;
import com.ssu.urlshortener.global.exception.url.ShortCodeGenerationException;
import com.ssu.urlshortener.global.exception.url.UrlExpiredException;
import com.ssu.urlshortener.global.exception.url.UrlNotFoundException;
import com.ssu.urlshortener.url.dto.CreateUrlRequest;
import com.ssu.urlshortener.url.dto.UrlResponse;
import com.ssu.urlshortener.url.entity.Url;
import com.ssu.urlshortener.url.generator.ShortCodeGenerator;
import com.ssu.urlshortener.url.repository.UrlRepository;

import lombok.RequiredArgsConstructor;
import com.ssu.urlshortener.global.exception.url.InvalidUrlUpdateException;
import com.ssu.urlshortener.url.dto.UpdateUrlRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UrlService {

	private static final int MAX_GENERATION_ATTEMPTS = 10;

	private final UrlRepository urlRepository;
	private final ShortCodeGenerator shortCodeGenerator;

	@Value("${app.base-url}")
	private String baseUrl;

	@Transactional
	public UrlResponse create(CreateUrlRequest request) {
		String shortCode = generateUniqueShortCode();

		Url url = Url.builder()
				.originalUrl(request.originalUrl())
				.shortCode(shortCode)
				.expiresAt(request.expiresAt())
				.build();

		Url savedUrl = urlRepository.save(url);

		return UrlResponse.from(savedUrl, baseUrl);
	}

	private String generateUniqueShortCode() {
		for (int i = 0; i < MAX_GENERATION_ATTEMPTS; i++) {
			String shortCode = shortCodeGenerator.generate();

			if (!urlRepository.existsByShortCode(shortCode)) {
				return shortCode;
			}
		}

		throw new ShortCodeGenerationException();
	}

	@Transactional
	public String redirect(String shortCode) {

		Url url = urlRepository.findByShortCode(shortCode)
				.orElseThrow(UrlNotFoundException::new);

		if (url.isExpired(LocalDateTime.now())) {
			throw new UrlExpiredException();
		}

		url.increaseClickCount();

		return url.getOriginalUrl();
	}
	
	public PageResponse<UrlResponse> getUrls(
			String keyword,
			Pageable pageable
	) {
		Page<Url> urlPage;

		if (keyword == null || keyword.isBlank()) {
			urlPage = urlRepository.findAll(pageable);
		} else {
			String normalizedKeyword = keyword.trim();

			urlPage =
					urlRepository
							.findByOriginalUrlContainingIgnoreCaseOrShortCodeContainingIgnoreCase(
									normalizedKeyword,
									normalizedKeyword,
									pageable
							);
		}

		Page<UrlResponse> responsePage = urlPage
				.map(url -> UrlResponse.from(url, baseUrl));

		return PageResponse.from(responsePage);
	}
	
	public UrlResponse getUrl(String shortCode) {

		Url url = urlRepository.findByShortCode(shortCode)
				.orElseThrow(UrlNotFoundException::new);

		return UrlResponse.from(url, baseUrl);
	}
	
	@Transactional
	public void delete(String shortCode) {

		Url url = urlRepository.findByShortCode(shortCode)
				.orElseThrow(UrlNotFoundException::new);

		urlRepository.delete(url);
	}
	
	@Transactional
	public UrlResponse update(String shortCode, UpdateUrlRequest request) {
		if (request.originalUrl() == null && request.expiresAt() == null) {
			throw new InvalidUrlUpdateException();
		}

		Url url = urlRepository.findByShortCode(shortCode)
				.orElseThrow(UrlNotFoundException::new);

		String originalUrl = request.originalUrl();

		if (originalUrl != null) {
			originalUrl = originalUrl.trim();
		}

		url.update(originalUrl, request.expiresAt());

		return UrlResponse.from(url, baseUrl);
	}
}