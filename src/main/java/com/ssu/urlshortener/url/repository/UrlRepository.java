package com.ssu.urlshortener.url.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ssu.urlshortener.url.entity.Url;

public interface UrlRepository extends JpaRepository<Url, Long> {

	Optional<Url> findByShortCode(String shortCode);

	boolean existsByShortCode(String shortCode);

	Page<Url> findByOriginalUrlContainingIgnoreCaseOrShortCodeContainingIgnoreCase(
			String originalUrl,
			String shortCode,
			Pageable pageable
	);

	@Modifying(
			clearAutomatically = true,
			flushAutomatically = true
	)
	@Query("""
			delete from Url u
			where u.expiresAt is not null
			  and u.expiresAt < :now
			""")
	int deleteExpiredUrls(@Param("now") LocalDateTime now);
}