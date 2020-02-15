package com.felipegabriel.libraryapi.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.felipegabriel.libraryapi.api.exception.BusinessException;
import com.felipegabriel.libraryapi.api.model.entity.Book;
import com.felipegabriel.libraryapi.api.model.repository.BookRepository;
import com.felipegabriel.libraryapi.api.service.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {
	
	BookService service;
	
	@MockBean
	BookRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new BookServiceImpl(repository);
	}
	
	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		//cenario
		Book book = Book.builder().isbn("123").author("Felipe").title("As aventuras").build();
		Mockito.when(repository.save(book)).thenReturn(
				Book.builder().id(1l)
							  .isbn("123")
							  .author("Felipe")
							  .title("As aventuras").build());
		//execucao
		Book savedBook = service.save(book);
		
		//verificacao
		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("123");
		assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
		assertThat(savedBook.getAuthor()).isEqualTo("Felipe");
	}
	
	private Book createValidBook() {
		return Book.builder().isbn("123").author("Felipe").title("As aventuras").build();
	}
	
	@Test
	@DisplayName("Deve lançar um erro de negocio ao tentar salvar um livro com isbn duplicado")
	public void shouldNotSaveBookWithDuplicatedISBN() {
		
		// cenario
		Book book = createValidBook();
		Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
		
		// execução
		Throwable exception = Assertions.catchThrowable(() -> service.save(book));
		
		// verificações
		assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage("Isbn já cadastrado.");
		
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	@Test
	@DisplayName("Deve obter um livro por Id")
	public void getByIdTest() {
		Long id = 1l;
		Book book = createValidBook();
		book.setId(1l);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));
		
		// execucao
		Optional<Book> foundBook = service.getById(id);
		
		// verificacoes
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
		
		
	}
	
	@Test
	@DisplayName("Deve retornar vazio ao obter um livro por id quando ele não existe")
	public void bookNotFoundByIdTest() {
		Long id = 1l;
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		// execucao
		Optional<Book> book = service.getById(id);
		
		// verificacoes
		assertThat(book.isPresent()).isFalse();
		
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() {
		// cenario
		Book book = Book.builder().id(1l).build();
		
		// execucao
		assertDoesNotThrow(() -> service.delete(book));
		
		// verificacoes
		Mockito.verify(repository, Mockito.times(1)).delete(book);
	}
		
	@Test
	@DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente")
	public void deleteInvalidBookTest() {
		Book book = new Book();
		assertThrows(IllegalArgumentException.class, () ->  service.delete(book));
		Mockito.verify(repository, Mockito.never()).delete(book);
	}
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() {
		// cenario
		long id = 1l;
		
		//livro a atualizar
		Book updatingBook = Book.builder().id(id).build();
		
		// simulação
		Book updatedBook = createValidBook();
		updatedBook.setId(id);
		
		Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);
		
		// execução
		Book book = service.update(updatingBook);
		
		//verificações
		assertThat(book.getId()).isEqualTo(updatedBook.getId());
		assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
		assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
		assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
	}
	
	@Test
	@DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente")
	public void updateInvalidBookTest() {
		Book book = new Book();
		assertThrows(IllegalArgumentException.class, () ->  service.update(book));
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Deve filtrar livros pelas propriedades")
	public void findBookTest() {
		
		// cenario
		Book book = createValidBook();
		
		PageRequest pageRequest = PageRequest.of(0, 10);
		
		List<Book> lista = Arrays.asList(book);
		Page<Book> page = new PageImpl<Book>(Arrays.asList(book), pageRequest, 1 );
		Mockito.when(repository.findAll(Mockito.any(Example.class),
				Mockito.any(PageRequest.class)))
				.thenReturn(page);
		
		// execução
		Page<Book> result = service.find(book, pageRequest);
		
		// verificações
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(lista);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}
	
	@Test
	@DisplayName("Deve obter um livro pelo isbn")
	public void getBookByIsbnTest() {
		String isbn = "1230";
		Mockito.when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1l).isbn(isbn).build()));
		Optional<Book> book = service.getBookByIsbn(isbn);
		
		assertThat(book.isPresent()).isTrue();
		assertThat(book.get().getId()).isEqualTo(1l);
		assertThat(book.get().getIsbn()).isEqualTo(isbn);
		
		verify(repository, Mockito.times(1)).findByIsbn(isbn);
	}
	
	
}
