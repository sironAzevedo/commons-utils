package com.br.azevedo.exception;


import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 236801744437352250L;

	public BusinessException(String message, Throwable cause) {
		super(i18n(message), cause);
	}

	public BusinessException(String message) {
		super(i18n(message));
	}

	public BusinessException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}

	public BusinessException(Throwable cause, String messageKey, Object... values) {
		super(i18n(messageKey, values), cause);
	}

}
