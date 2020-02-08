package com.felipegabriel.libraryapi.api.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;

import lombok.Getter;

public class ApiErros {
	
	@Getter
	private List<String> erros;
	
	public ApiErros(BindingResult bindingResult) {
		this.erros = new ArrayList<>();
		bindingResult.getAllErrors().forEach(error -> this.erros.add(error.getDefaultMessage()));
	}
	
	
}
