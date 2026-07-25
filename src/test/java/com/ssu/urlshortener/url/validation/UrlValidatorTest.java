package com.ssu.urlshortener.url.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UrlValidatorTest {

	private final UrlValidator validator = new UrlValidator();

	@Nested
	@DisplayName("유효한 URL")
	class Valid {

		@Test
		@DisplayName("HTTPS URL을 허용한다")
		void httpsUrl() {
			boolean result = validator.isValid(
					"https://example.com/path?q=spring",
					null
			);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("HTTP URL을 허용한다")
		void httpUrl() {
			boolean result = validator.isValid(
					"http://example.com",
					null
			);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("포트가 포함된 URL을 허용한다")
		void urlWithPort() {
			boolean result = validator.isValid(
					"http://localhost:8081/test",
					null
			);

			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("null은 다른 제약조건에 맡기기 위해 허용한다")
		void nullValue() {
			boolean result = validator.isValid(null, null);

			assertThat(result).isTrue();
		}
	}

	@Nested
	@DisplayName("유효하지 않은 URL")
	class Invalid {

		@Test
		@DisplayName("프로토콜이 없으면 거부한다")
		void withoutScheme() {
			boolean result = validator.isValid(
					"example.com",
					null
			);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("HTTP와 HTTPS 이외의 프로토콜은 거부한다")
		void invalidScheme() {
			boolean result = validator.isValid(
					"ftp://example.com",
					null
			);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("호스트가 없으면 거부한다")
		void withoutHost() {
			boolean result = validator.isValid(
					"https://",
					null
			);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("공백이 포함된 URL은 거부한다")
		void whitespace() {
			boolean result = validator.isValid(
					"https://example .com",
					null
			);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("앞뒤 공백이 있으면 거부한다")
		void surroundingWhitespace() {
			boolean result = validator.isValid(
					" https://example.com ",
					null
			);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("사용자 정보가 포함된 URL은 거부한다")
		void userInfo() {
			boolean result = validator.isValid(
					"https://user:password@example.com",
					null
			);

			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("빈 문자열은 거부한다")
		void empty() {
			boolean result = validator.isValid("", null);

			assertThat(result).isFalse();
		}
	}
}