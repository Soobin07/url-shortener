package com.ssu.urlshortener.url.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UrlValidator
		implements ConstraintValidator<ValidUrl, String> {

	@Override
	public boolean isValid(
			String value,
			ConstraintValidatorContext context
	) {
		/*
		 * null 여부는 @NotBlank, @NotNull 등 다른 Annotation이 담당합니다.
		 *
		 * UpdateUrlRequest에서는 null이
		 * "해당 필드를 수정하지 않는다"는 의미이므로 허용합니다.
		 */
		if (value == null) {
			return true;
		}

		if (value.isBlank()) {
			return false;
		}

		/*
		 * 앞뒤 공백이나 URL 중간의 공백을 허용하지 않습니다.
		 */
		if (!value.equals(value.trim())
				|| value.chars().anyMatch(Character::isWhitespace)) {
			return false;
		}

		try {
			URI uri = new URI(value);

			String scheme = uri.getScheme();
			String host = uri.getHost();

			if (scheme == null || host == null || host.isBlank()) {
				return false;
			}

			String normalizedScheme =
					scheme.toLowerCase(Locale.ROOT);

			if (!normalizedScheme.equals("http")
					&& !normalizedScheme.equals("https")) {
				return false;
			}

			/*
			 * 사용자명과 비밀번호가 포함된 URL은 허용하지 않습니다.
			 * 예: https://user:password@example.com
			 */
			if (uri.getUserInfo() != null) {
				return false;
			}

			return true;

		} catch (URISyntaxException exception) {
			return false;
		}
	}
}