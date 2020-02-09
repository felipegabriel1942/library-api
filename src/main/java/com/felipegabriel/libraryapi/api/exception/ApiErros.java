package com.felipegabriel.libraryapi.api.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;

public class ApiErros {
	
	@Getter
	private List<String> erros;
	
	public ApiErros(BindingResult bindingResult) {
		this.erros = new ArrayList<>();
		bindingResult.getAllErrors().forEach(error -> this.erros.add(error.getDefaultMessage()));
	}
	
	public ApiErros(BusinessException ex) {
		this.erros = Arrays.asList(ex.getMessage());
	}
	
	public ApiErros(ResponseStatusException ex) {
		this.erros = Arrays.asList(ex.getReason());
	}
	
}
