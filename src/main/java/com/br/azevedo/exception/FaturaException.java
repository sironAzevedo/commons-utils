package com.br.azevedo.exception;

import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class FaturaException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FaturaException(String message) {
		super(message);
	}

	public FaturaException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}
}
