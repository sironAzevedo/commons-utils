/*
 * Classe responsável por configurar a exceção de conflitos de viagem.
 * 
 * © . Copyright. - 2020 Porto Seguro. S.A. - Todos os direitos reservados.
 */
package com.br.azevedo.exception;


import static com.br.azevedo.utils.mensagemUtils.MessageUtils.i18n;

public class ConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConflictException(String message, Throwable cause) {
		super(i18n(message), cause);
	}

	public ConflictException(String message) {
		super(i18n(message));
	}

	public ConflictException(String messageKey, Object... values) {
		super(i18n(messageKey, values));
	}

	public ConflictException(Throwable cause, String messageKey, Object... values) {
		super(i18n(messageKey, values), cause);
	}

}