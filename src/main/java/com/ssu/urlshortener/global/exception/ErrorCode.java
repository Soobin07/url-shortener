package com.ssu.urlshortener.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	URL_NOT_FOUND(HttpStatus.NOT_FOUND, "URL_001", "존재하지 않는 단축 URL입니다."),

	URL_EXPIRED(HttpStatus.GONE, "URL_002", "만료된 단축 URL입니다."),

	SHORT_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "URL_003", "단축 코드 생성에 실패했습니다."),

	INVALID_URL_UPDATE_REQUEST(HttpStatus.BAD_REQUEST, "URL_004", "수정할 URL 정보가 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}