package com.yonathan.featureflags.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yonathan.featureflags.service.FeatureFlagAlreadyExistsException;
import com.yonathan.featureflags.service.FeatureFlagNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(FeatureFlagAlreadyExistsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ProblemDetail handleFeatureFlagAlreadyExists(FeatureFlagAlreadyExistsException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(FeatureFlagNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ProblemDetail handleFeatureFlagNotFound(FeatureFlagNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}
}
