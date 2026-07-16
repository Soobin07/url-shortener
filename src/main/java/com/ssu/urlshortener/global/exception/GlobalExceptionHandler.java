package com.ssu.urlshortener.global.exception;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(
			BusinessException e
	) {

		ErrorCode errorCode = e.getErrorCode();

		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.body(
						new ErrorResponse(
								LocalDateTime.now(),
								errorCode.getHttpStatus().value(),
								errorCode.getCode(),
								errorCode.getMessage()
						)
				);
	}
}