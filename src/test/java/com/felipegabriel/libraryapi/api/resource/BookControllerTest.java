package com.felipegabriel.libraryapi.api.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipegabriel.libraryapi.api.dto.BookDTO;
import com.felipegabriel.libraryapi.api.exception.BusinessException;
import com.felipegabriel.libraryapi.api.model.entity.Book;
import com.felipegabriel.libraryapi.api.service.BookService;
import com.felipegabriel.libraryapi.api.service.LoanService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = BookController.class)
public class BookControllerTest {
	
	static String BOOK_API = "/api/books";
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookService service;
	
	@MockBean
	LoanService loanService;
	
	@Test
	@DisplayName("Deve criar um livro com sucesso.")
	public void createBookTest() throws Exception {
		
		BookDTO dto = BookDTO.builder().author("Felipe").title("As aventuras").isbn("001").build();
		Book savedBook = Book.builder().id(10l).author("Felipe").title("As aventuras").isbn("001").build();
		
		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.post(BOOK_API)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.jsonPath("id").value(10l))
			.andExpect(MockMvcResultMatchers.jsonPath("title").value(dto.getTitle()))
			.andExpect(MockMvcResultMatchers.jsonPath("author").value(dto.getAuthor()))
			.andExpect(MockMvcResultMatchers.jsonPath("isbn").value(dto.getIsbn()));
	}
	
	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficientes para a criação do livro.")
	public void createInvalidBookTest() throws Exception{
		String json = new ObjectMapper().writeValueAsString(new BookDTO());
				
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isBadRequest())
			.andExpect(jsonPath("erros", Matchers.hasSize(3)));
			
		
	}
	
	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar um livro com isbn já utilizado por outro.")
	public void createBookWithDuplicatedIsbn() throws Exception {
		
		BookDTO dto = createNewBook();
		String json = new ObjectMapper().writeValueAsString(dto);
		String mensagemErro = "Isbn já cadastrado.";
		BDDMockito.given(service.save(Mockito.any(Book.class)))
			.willThrow(new BusinessException(mensagemErro));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(MockMvcResultMatchers.jsonPath("erros", Matchers.hasSize(1)))
				.andExpect(MockMvcResultMatchers.jsonPath("erros[0]").value(mensagemErro));
	}
	
	
	@Test
	@DisplayName("Deve obter informações de um livro.")
	public void getBookDetailsTest() throws Exception {
		
		// Cenario (given)
		Long id =1l;
		
		Book book = Book.builder()
				.id(id)
				.title(createNewBook().getTitle())
				.author(createNewBook().getAuthor())
				.isbn(createNewBook().getIsbn())
				.build();
		
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));
		
		// Execução (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
			.andExpect(MockMvcResultMatchers.jsonPath("title").value(createNewBook().getTitle()))
			.andExpect(MockMvcResultMatchers.jsonPath("author").value(createNewBook().getAuthor()))
			.andExpect(MockMvcResultMatchers.jsonPath("isbn").value(createNewBook().getIsbn()));
	}
	
	@Test
	@DisplayName("Deve retornar resource not found quando o livro procurado não existir")
	public void bookNotFoundTest() throws Exception {
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
		
		// Execução (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.
			perform(request)
			.andExpect(MockMvcResultMatchers.status().isNotFound());
				
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() throws Exception {
		
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));
		
		// Execução (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/" + 1));
		
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}
	
	@Test
	@DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
	public void deleteInexistentBookTest() throws Exception {
		
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
		
		// Execução (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/" + 1));
		
		mvc.perform(request)
				.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() throws Exception {
		Long id = 1l;
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		
		Book updatingBook = Book.builder()
				.id(1l)
				.title("some title")
				.author("Jose")
				.isbn("321").build();
		
		BDDMockito.given(service.getById(id))
				.willReturn(Optional.of(updatingBook));
		Book updatedBook = Book.builder()
				.id(1l)
				.title("As aventuras")
				.author("Felipe")
				.isbn("321").build();
		BDDMockito.given(service.update(updatingBook))
				.willReturn(updatedBook);
		
		// Execução (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + 1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
			.andExpect(MockMvcResultMatchers.jsonPath("title").value(createNewBook().getTitle()))
			.andExpect(MockMvcResultMatchers.jsonPath("author").value(createNewBook().getAuthor()))
			.andExpect(MockMvcResultMatchers.jsonPath("isbn").value("321"));
	}
	
	@Test
	@DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
	public void updateInexistentBookTest() throws Exception {
		String json = new ObjectMapper().writeValueAsString(createNewBook());
		BDDMockito.given(service.getById(Mockito.anyLong()))
				.willReturn(Optional.empty());
		
		// Execução (when)
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + 1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);
		
		mvc
			.perform(request)
			.andExpect(MockMvcResultMatchers.status().isNotFound());
			
	}
	
	@Test
	@DisplayName("Deve filtrar livros")
	public void findBooksTest() throws Exception {
		Long id = 1l;
		
		Book book = Book.builder()
				.id(id)
				.title(createNewBook().getTitle())
				.isbn(createNewBook().getIsbn())
				.author(createNewBook().getAuthor())
				.build();
		
		BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
			.willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));
		
		String queryString = String.format("?title=%s&author=%s&page=0&size=100",
				book.getTitle(), book.getAuthor());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.get(BOOK_API.concat(queryString))
			.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
			.andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(100))
			.andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0));
	}
	
	private BookDTO createNewBook() {
		return BookDTO.builder().author("Felipe").title("As aventuras").isbn("001").build();
	}
}
