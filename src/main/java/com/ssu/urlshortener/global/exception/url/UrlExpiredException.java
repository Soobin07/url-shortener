package com.ssu.urlshortener.global.exception.url;

import com.ssu.urlshortener.global.exception.BusinessException;
import com.ssu.urlshortener.global.exception.ErrorCode;

public class UrlExpiredException extends BusinessException {

	public UrlExpiredException() {
		super(ErrorCode.URL_EXPIRED);
	}
}