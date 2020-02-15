package com.felipegabriel.libraryapi.api.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.felipegabriel.libraryapi.api.dto.LoanFilterDTO;
import com.felipegabriel.libraryapi.api.model.entity.Loan;


public interface LoanService {

	Loan save(Loan loan);

	Optional<Loan> getById(Long id);

	Loan update(Loan loan);

	Page<Loan> find(LoanFilterDTO any, Pageable any2);

}
