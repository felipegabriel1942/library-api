package com.felipegabriel.libraryapi.api.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.felipegabriel.libraryapi.api.model.entity.Book;
import com.felipegabriel.libraryapi.api.model.entity.Loan;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {
	
	
	@Autowired
	private LoanRepository repository;
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Test
	@DisplayName("Deve verificar se existe empréstimo não devolvido para o livro.")
	public void existisByBookAndNotReturnedTest() {
		
		// cenário
		Loan loan = createAndPersistLoan();
		Book book = loan.getBook();
		
		// execucao
		boolean exists = repository.existsByBookAndNotReturned(book);
		
		assertThat(exists).isTrue();
	}
	
	
	@Test
	@DisplayName("Deve buscar empréstimo pelo isbn do livro ou costumer")
	public void findByBookIsbnOrCostumerTest() {
		Loan loan = createAndPersistLoan();
		Page<Loan> result = repository.findByBookIsbnOrCostumer("123", "Fulano", PageRequest.of(0, 10));
		
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent()).contains(loan);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);
	}
	
	
	public Loan createAndPersistLoan() {
		Book book = BookRepositoryTest.createNewBook("123");
		entityManager.persist(book);
		
		Loan loan = Loan.builder().book(book).costumer("Fulano").loanDate(LocalDate.now()).build();
		entityManager.persist(loan);
		
		return loan;
	}
	
}
