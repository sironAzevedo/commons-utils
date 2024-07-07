package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class InternalErrorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InternalErrorException(String message) {
		super(message);
	}

	public InternalErrorException(Exception e) {
		super(e);
	}

	public InternalErrorException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}
}
