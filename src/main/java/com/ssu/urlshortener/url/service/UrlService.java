package com.ssu.urlshortener.url.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssu.urlshortener.url.dto.CreateUrlRequest;
import com.ssu.urlshortener.url.dto.UrlResponse;
import com.ssu.urlshortener.url.entity.Url;
import com.ssu.urlshortener.url.generator.ShortCodeGenerator;
import com.ssu.urlshortener.url.repository.UrlRepository;

import lombok.RequiredArgsConstructor;

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

		throw new IllegalStateException("단축 코드 생성에 실패했습니다.");
	}
	
	@Transactional
	public String redirect(String shortCode) {
		Url url = urlRepository.findByShortCode(shortCode)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단축 URL입니다."));

		if (url.isExpired(LocalDateTime.now())) {
			throw new IllegalStateException("만료된 단축 URL입니다.");
		}

		url.increaseClickCount();

		return url.getOriginalUrl();
	}
}