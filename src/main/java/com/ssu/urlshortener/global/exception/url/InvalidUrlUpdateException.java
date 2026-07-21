package com.ssu.urlshortener.global.exception.url;

import com.ssu.urlshortener.global.exception.BusinessException;
import com.ssu.urlshortener.global.exception.ErrorCode;

public class InvalidUrlUpdateException extends BusinessException {

	public InvalidUrlUpdateException() {
		super(ErrorCode.INVALID_URL_UPDATE_REQUEST);
	}
}