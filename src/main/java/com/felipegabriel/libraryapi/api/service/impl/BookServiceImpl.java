package com.felipegabriel.libraryapi.api.service.impl;

import org.springframework.stereotype.Service;

import com.felipegabriel.libraryapi.api.model.entity.Book;
import com.felipegabriel.libraryapi.api.model.repository.BookRepository;
import com.felipegabriel.libraryapi.api.service.BookService;

@Service
public class BookServiceImpl implements BookService {
	
	private BookRepository repository;
	
	public BookServiceImpl(BookRepository repository) {
		this.repository = repository;
	}
	
	@Override
	public Book save(Book book) {
		return repository.save(book);
	}

}
