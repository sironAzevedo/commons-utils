package com.br.azevedo.exception;


import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = 6347141027459329584L;

	public ApplicationException(String message, Throwable cause) {
		super(i18n(message), cause);
	}

	public ApplicationException(String message) {
		super(i18n(message));
	}

	public ApplicationException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}

	public ApplicationException(Throwable cause, String messageKey, Object... values) {
		super(i18n(messageKey, values), cause);
	}

}
