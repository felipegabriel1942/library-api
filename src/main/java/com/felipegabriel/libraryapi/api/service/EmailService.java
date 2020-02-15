package com.felipegabriel.libraryapi.api.service;

import java.util.List;

public interface EmailService {

	void sendMails(String mensagem, List<String> mailList);

}
