package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class NotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}
}
