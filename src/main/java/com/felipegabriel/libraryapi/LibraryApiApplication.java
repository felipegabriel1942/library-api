package com.felipegabriel.libraryapi;

import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.felipegabriel.libraryapi.api.service.EmailService;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {
	
	@Autowired
	private EmailService emailService;
	
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
	@Bean
	public CommandLineRunner runner() {
		return args -> {
			List<String> emails = Arrays.asList("library-api-435333@inbox.mailtrap.io");
			emailService.sendMails("Testando serviço de emails", emails);
		};
	}
		
	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
