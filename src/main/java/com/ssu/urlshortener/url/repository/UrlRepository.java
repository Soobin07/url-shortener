package com.ssu.urlshortener.url.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssu.urlshortener.url.entity.Url;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

}