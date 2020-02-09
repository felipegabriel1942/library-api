package com.felipegabriel.libraryapi.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.felipegabriel.libraryapi.api.exception.BusinessException;
import com.felipegabriel.libraryapi.api.model.entity.Book;
import com.felipegabriel.libraryapi.api.model.entity.Loan;
import com.felipegabriel.libraryapi.api.model.repository.LoanRepository;
import com.felipegabriel.libraryapi.api.service.impl.LoanServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {
	
	LoanService service;
	
	@MockBean
	LoanRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new LoanServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar um empréstimo")
	public void saveLoanTest() {
		
		Book book = Book.builder().id(1l).build();
		String costumer = "Fulano";
		
		Loan savingLoan = Loan.builder()
				.book(book)
				.costumer(costumer)
				.loanDate(LocalDate.now())
				.build();
		
		Loan savedLoan = Loan.builder()
				.id(1l)
				.loanDate(LocalDate.now())
				.costumer(costumer)
				.book(book)
				.build();
		
		BDDMockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
		BDDMockito.when(repository.save(savingLoan)).thenReturn(savedLoan);
		
		Loan loan = service.save(savingLoan);
		
		assertThat(loan.getId()).isEqualTo(savedLoan.getId());
		assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
		assertThat(loan.getCostumer()).isEqualTo(savedLoan.getCostumer());
		assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
		
	}
	
	@Test
	@DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado")
	public void loanedBookSaveTest() {
		
		Book book = Book.builder().id(1l).build();
		String costumer = "Fulano";
		
		Loan savingLoan = Loan.builder()
				.book(book)
				.costumer(costumer)
				.loanDate(LocalDate.now())
				.build();
		
		when(repository.existsByBookAndNotReturned(book)).thenReturn(true);
		
		Throwable exception = catchThrowable(() -> service.save(savingLoan));
		
		assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage("Book already loaned");
		
		verify(repository, Mockito.never()).save(savingLoan);
		
		
		
	}


}
