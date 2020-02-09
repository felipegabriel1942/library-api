package com.felipegabriel.libraryapi.api.service;

import java.util.Optional;

import com.felipegabriel.libraryapi.api.model.entity.Loan;

public interface LoanService {

	Loan save(Loan loan);

	Optional<Loan> getById(Long id);

	Loan update(Loan loan);

}
