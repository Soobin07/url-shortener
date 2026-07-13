package com.ssu.urlshortener.url.util;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

	private static final String CHARACTERS =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final int CODE_LENGTH = 7;

	private final SecureRandom random = new SecureRandom();

	public String generate() {
		StringBuilder builder = new StringBuilder(CODE_LENGTH);

		for (int i = 0; i < CODE_LENGTH; i++) {
			int index = random.nextInt(CHARACTERS.length());
			builder.append(CHARACTERS.charAt(index));
		}

		return builder.toString();
	}
}