package com.ssu.urlshortener.url.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({
		FIELD,
		PARAMETER,
		RECORD_COMPONENT,
		ANNOTATION_TYPE
})
@Retention(RUNTIME)
public @interface ValidUrl {

	String message() default "올바른 HTTP 또는 HTTPS URL을 입력해 주세요.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}