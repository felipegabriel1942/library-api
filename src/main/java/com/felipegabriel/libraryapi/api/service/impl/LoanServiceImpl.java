package com.felipegabriel.libraryapi.api.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.felipegabriel.libraryapi.api.dto.LoanFilterDTO;
import com.felipegabriel.libraryapi.api.exception.BusinessException;
import com.felipegabriel.libraryapi.api.model.entity.Loan;
import com.felipegabriel.libraryapi.api.model.repository.LoanRepository;
import com.felipegabriel.libraryapi.api.service.LoanService;

@Service
public class LoanServiceImpl implements LoanService{

	private LoanRepository repository;
	
	public LoanServiceImpl(LoanRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public Loan save(Loan loan) {
		if(repository.existsByBookAndNotReturned(loan.getBook())) {
			throw new BusinessException("Book already loaned");
		}
		return repository.save(loan);
	}

	@Override
	public Optional<Loan> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Loan update(Loan loan) {
		return repository.save(loan);
	}

	@Override
	public Page<Loan> find(LoanFilterDTO filterDTO, Pageable page) {
		return repository.findByBookIsbnOrCostumer(filterDTO.getIsbn(),
				filterDTO.getCostumer(), page);
	}
	
}
