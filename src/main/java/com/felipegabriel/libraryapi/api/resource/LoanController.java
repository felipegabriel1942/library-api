package com.felipegabriel.libraryapi.api.resource;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.felipegabriel.libraryapi.api.dto.LoanDTO;
import com.felipegabriel.libraryapi.api.dto.ReturnedLoanDTO;
import com.felipegabriel.libraryapi.api.model.entity.Book;
import com.felipegabriel.libraryapi.api.model.entity.Loan;
import com.felipegabriel.libraryapi.api.service.BookService;
import com.felipegabriel.libraryapi.api.service.LoanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
	
	private final LoanService service;
	private final BookService bookService;
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody LoanDTO dto) {
		
		Book book = bookService.getBookByIsbn(dto.getIsbn())
				.orElseThrow(() -> 
					new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
		
		Loan entity = Loan.builder()
				.book(book)
				.costumer(dto.getCustomer())
				.loanDate(LocalDate.now())
				.build();
		
		entity = service.save(entity);
		
		return entity.getId();
	}
	
	@PatchMapping("{id}")
	public void returnBook(
			@PathVariable Long id,
			@RequestBody ReturnedLoanDTO dto) {
		Loan loan = service.getById(id).orElseThrow(() ->
		new ResponseStatusException(HttpStatus.NOT_FOUND));
		loan.setReturned(dto.getReturned());
		service.update(loan);
	}
}