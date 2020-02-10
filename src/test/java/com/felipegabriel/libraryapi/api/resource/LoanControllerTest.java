package com.felipegabriel.libraryapi.api.resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.time.LocalDate;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipegabriel.libraryapi.api.dto.LoanDTO;
import com.felipegabriel.libraryapi.api.dto.ReturnedLoanDTO;
import com.felipegabriel.libraryapi.api.exception.BusinessException;
import com.felipegabriel.libraryapi.api.model.entity.Book;
import com.felipegabriel.libraryapi.api.model.entity.Loan;
import com.felipegabriel.libraryapi.api.service.BookService;
import com.felipegabriel.libraryapi.api.service.LoanService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {
	
	static final String LOAN_API = "/api/loans";
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	private BookService bookService;
	
	@MockBean
	private LoanService loanService;
	
	@Test
	@DisplayName("Deve realizar um emprestimo")
	public void createLoanTest() throws Exception {
		
		LoanDTO dto = LoanDTO.builder()
				.isbn("123")
				.customer("Fulano").build();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		Book book = Book.builder().id(1l).isbn("123").build();
		
		BDDMockito.given(bookService.getBookByIsbn("123"))
			.willReturn(Optional.of(book));
		
		Loan loan = Loan.builder().id(1l)
				.costumer("Fulano")
				.book(book)
				.loanDate(LocalDate.now())
				.build();
		
		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.content().string("1"));
			
	}
	
	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente.")
	public void invalidIsbnCreateLoanTest() throws Exception{
		
		LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		BDDMockito.given(bookService.getBookByIsbn("123"))
		.willReturn(Optional.empty());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isBadRequest())
			.andExpect(MockMvcResultMatchers.jsonPath("erros", Matchers.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("erros[0]").value("Book not found for passed isbn"));
	}
	
	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro já emprestado.")
	public void loanedBookErrorOnCreateLoanTest() throws Exception{
		
		LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		Book book = Book.builder().id(1l).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123"))
			.willReturn(Optional.of(book));
		
		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))	
			.willThrow(new BusinessException("Book already loaned"));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API)
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isBadRequest())
			.andExpect(MockMvcResultMatchers.jsonPath("erros", Matchers.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("erros[0]").value("Book already loaned"));
	}
	
	@Test
	@DisplayName("Deve retornar um livro")
	public void returnBookTest() throws Exception {
		// cenario
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
		Loan loan = Loan.builder().id(1l).build();
		BDDMockito.given(loanService.getById(Mockito.anyLong()))
			.willReturn(Optional.of(loan));
		
		String json = new ObjectMapper().writeValueAsString(dto);
		
		mvc.perform(
				patch(LOAN_API.concat("/1"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
		.andExpect(MockMvcResultMatchers.status().isOk());
		
		Mockito.verify(loanService, Mockito.times(1)).update(loan);
				
	}
	
	@Test
	@DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
	public void returnInexistentBookTest() throws Exception {
		// cenario
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder()
			.returned(true).build();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		BDDMockito.given(loanService.getById(Mockito.anyLong()))
			.willReturn(Optional.empty());
				
		mvc.perform(
				patch(LOAN_API.concat("/1"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
		.andExpect(MockMvcResultMatchers.status().isNotFound());
			
	}
}