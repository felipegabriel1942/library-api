package com.felipegabriel.libraryapi.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.felipegabriel.libraryapi.api.exception.ApiErros;
import com.felipegabriel.libraryapi.api.exception.BusinessException;

@RestControllerAdvice
public class ApplicationControllerAdvice {
	
	// Metodos para tratar errors
		@ExceptionHandler(MethodArgumentNotValidException.class)
		@ResponseStatus(HttpStatus.BAD_REQUEST)
		public ApiErros handleValidationExceptions(MethodArgumentNotValidException exception) {
			BindingResult bindingResult = exception.getBindingResult();
			return new ApiErros(bindingResult);
		}
		
		@ExceptionHandler(BusinessException.class)
		@ResponseStatus(HttpStatus.BAD_REQUEST)
		public ApiErros handlerBusinessException(BusinessException exception) {
			return new ApiErros(exception);
		}
		
		
		@ExceptionHandler(ResponseStatusException.class)
		@ResponseStatus
		public ResponseEntity handleResponseStatusException(ResponseStatusException exception) {
			return new ResponseEntity(new ApiErros(exception), exception.getStatus());
		}
}
