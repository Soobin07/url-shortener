package com.ssu.urlshortener.global.exception.url;

import com.ssu.urlshortener.global.exception.BusinessException;
import com.ssu.urlshortener.global.exception.ErrorCode;

public class UrlNotFoundException extends BusinessException {

	public UrlNotFoundException() {
		super(ErrorCode.URL_NOT_FOUND);
	}
}