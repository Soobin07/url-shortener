package com.ssu.urlshortener.global.exception.url;

import com.ssu.urlshortener.global.exception.BusinessException;
import com.ssu.urlshortener.global.exception.ErrorCode;

public class ShortCodeGenerationException extends BusinessException {

	public ShortCodeGenerationException() {
		super(ErrorCode.SHORT_CODE_GENERATION_FAILED);
	}
}