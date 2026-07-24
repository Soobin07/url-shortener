package com.ssu.urlshortener.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI urlShortenerOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("URL Shortener API")
						.description("""
								긴 URL을 짧은 코드로 변환하고 관리하는 REST API입니다.

								주요 기능:
								- 단축 URL 생성
								- 원본 URL 리다이렉트
								- URL 목록 및 검색
								- URL 단건 조회
								- URL 수정
								- URL 삭제
								""")
						.version("v1.0.0")
						.contact(new Contact()
								.name("SSU")
						)
						.license(new License()
								.name("Private Project")
						)
				)
				.components(new Components());
	}
}