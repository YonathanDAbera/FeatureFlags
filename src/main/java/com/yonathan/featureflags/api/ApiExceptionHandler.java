package com.yonathan.featureflags.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yonathan.featureflags.service.FeatureFlagAlreadyExistsException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(FeatureFlagAlreadyExistsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail handleFeatureFlagAlreadyExists(FeatureFlagAlreadyExistsException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}
}
