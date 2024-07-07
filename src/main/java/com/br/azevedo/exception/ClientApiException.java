package com.br.azevedo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;


public class ClientApiException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	@Getter
	private HttpStatus status;

	public ClientApiException(String message) {
		super(i18n(message));
	}

	public ClientApiException(String message, HttpStatus status) {
		super(i18n(message));
		this.status = status;
	}

	public ClientApiException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}

	public ClientApiException(String messageKey, HttpStatus status, Object... values) {
		super(i18n(messageKey, values));
		this.status = status;
	}

	public ClientApiException(Throwable cause, String messageKey, Object... values) {
		super(i18n(messageKey, values), cause);
	}

	public ClientApiException(Throwable cause, String messageKey, HttpStatus status, Object... values) {
		super(i18n(messageKey, values), cause);
		this.status = status;
	}
}
